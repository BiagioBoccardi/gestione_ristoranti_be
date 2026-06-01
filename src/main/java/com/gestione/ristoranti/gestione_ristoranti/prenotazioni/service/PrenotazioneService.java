package com.gestione.ristoranti.gestione_ristoranti.prenotazioni.service;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.Prenotazione;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Tavolo;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Utente;
import com.gestione.ristoranti.gestione_ristoranti.auth.repository.UtenteRepository;
import com.gestione.ristoranti.gestione_ristoranti.exception.ResourceNotFoundException;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.TavoloRepository;
import com.gestione.ristoranti.gestione_ristoranti.prenotazioni.dto.PrenotazioneRequest;
import com.gestione.ristoranti.gestione_ristoranti.prenotazioni.dto.PrenotazioneResponse;
import com.gestione.ristoranti.gestione_ristoranti.prenotazioni.repository.PrenotazioneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class PrenotazioneService {

    private final PrenotazioneRepository prenotazioneRepository;
    private final TavoloRepository tavoloRepository;
    private final UtenteRepository utenteRepository;
    private final EmailService emailService;

    public PrenotazioneService(PrenotazioneRepository prenotazioneRepository,
                               TavoloRepository tavoloRepository,
                               UtenteRepository utenteRepository,
                               EmailService emailService) {
        this.prenotazioneRepository = prenotazioneRepository;
        this.tavoloRepository = tavoloRepository;
        this.utenteRepository = utenteRepository;
        this.emailService = emailService;
    }

    /**
     * Crea una nuova prenotazione per l'utente identificato dall'email.
     * Verifica che il tavolo non sia già prenotato nella stessa fascia oraria (±89 minuti).
     * Invia email di conferma al completamento.
     *
     * @param request    dati della prenotazione (tavolo, data, ora, coperti, note)
     * @param emailUtente email dell'utente autenticato che effettua la prenotazione
     * @return prenotazione creata come DTO
     * @throws IllegalStateException se il tavolo è già occupato nella fascia oraria richiesta
     */
    public PrenotazioneResponse crea(PrenotazioneRequest request, String emailUtente) {
        Utente utente = utenteRepository.findByEmail(emailUtente)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));

        Tavolo tavolo = tavoloRepository.findById(request.getTavoloId())
                .orElseThrow(() -> new ResourceNotFoundException("Tavolo non trovato"));

        verificaDisponibilita(tavolo, request.getData(), request.getOra(), null);

        Prenotazione prenotazione = new Prenotazione(
                tavolo, utente, request.getData(), request.getOra(),
                request.getCoperti(), request.getNote());

        prenotazione = prenotazioneRepository.save(prenotazione);
        emailService.inviaConfermaPrenotazione(prenotazione);

        return PrenotazioneResponse.from(prenotazione);
    }

    public PrenotazioneResponse modifica(Long id, PrenotazioneRequest request, String emailUtente) {
        Prenotazione prenotazione = prenotazioneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prenotazione non trovata"));

        verificaProprietaOPermesso(prenotazione, emailUtente);

        Tavolo tavolo = tavoloRepository.findById(request.getTavoloId())
                .orElseThrow(() -> new ResourceNotFoundException("Tavolo non trovato"));

        verificaDisponibilita(tavolo, request.getData(), request.getOra(), id);

        prenotazione.setTavolo(tavolo);
        prenotazione.setData(request.getData());
        prenotazione.setOra(request.getOra());
        prenotazione.setCoperti(request.getCoperti());
        prenotazione.setNote(request.getNote());

        prenotazione = prenotazioneRepository.save(prenotazione);
        emailService.inviaAggiornamentiPrenotazione(prenotazione);

        return PrenotazioneResponse.from(prenotazione);
    }

    public void cancella(Long id, String emailUtente) {
        Prenotazione prenotazione = prenotazioneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prenotazione non trovata"));

        verificaProprietaOPermesso(prenotazione, emailUtente);
        emailService.inviaCancellazionePrenotazione(prenotazione);
        prenotazioneRepository.delete(prenotazione);
    }

    @Transactional(readOnly = true)
    public PrenotazioneResponse getById(Long id) {
        Prenotazione prenotazione = prenotazioneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prenotazione non trovata con id: " + id));
        return PrenotazioneResponse.from(prenotazione);
    }

    @Transactional(readOnly = true)
    public List<PrenotazioneResponse> getMiePrenotazioni(String emailUtente) {
        Utente utente = utenteRepository.findByEmail(emailUtente)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));
        return prenotazioneRepository.findByUtente(utente).stream()
                .map(PrenotazioneResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PrenotazioneResponse> getPrenotazioniPerData(LocalDate data) {
        return prenotazioneRepository.findByData(data).stream()
                .map(PrenotazioneResponse::from)
                .toList();
    }

    private void verificaDisponibilita(Tavolo tavolo, LocalDate data, LocalTime ora, Long escludiId) {
        LocalTime oraInizio = ora.minusMinutes(89);
        LocalTime oraFine = ora.plusMinutes(89);

        boolean conflitto = prenotazioneRepository
                .findByTavoloAndData(tavolo, data)
                .stream()
                .filter(p -> escludiId == null || !p.getId().equals(escludiId))
                .anyMatch(p -> !p.getOra().isBefore(oraInizio) && !p.getOra().isAfter(oraFine));

        if (conflitto) {
            throw new IllegalStateException(
                "Tavolo " + tavolo.getNumero() + " già prenotato in questa fascia oraria");
        }
    }

    private void verificaProprietaOPermesso(Prenotazione prenotazione, String emailUtente) {
        Utente utente = utenteRepository.findByEmail(emailUtente)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));

        String ruolo = utente.getRuolo().getNome();
        boolean isProprietario = prenotazione.getUtente().getEmail().equals(emailUtente);
        boolean isStaff = ruolo.equals("ADMIN") || ruolo.equals("CAMERIERE");

        if (!isProprietario && !isStaff) {
            throw new SecurityException("Non autorizzato a modificare questa prenotazione");
        }
    }
}
