package com.gestione.ristoranti.gestione_ristoranti.staff.service;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.Ordine;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Prenotazione;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Ruolo;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Utente;
import com.gestione.ristoranti.gestione_ristoranti.auth.repository.RuoloRepository;
import com.gestione.ristoranti.gestione_ristoranti.auth.repository.UtenteRepository;
import com.gestione.ristoranti.gestione_ristoranti.exception.ResourceNotFoundException;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.OrdineRepository;
import com.gestione.ristoranti.gestione_ristoranti.prenotazioni.repository.PrenotazioneRepository;
import com.gestione.ristoranti.gestione_ristoranti.staff.dto.AggiornaStaffRequest;
import com.gestione.ristoranti.gestione_ristoranti.staff.dto.StaffDetailResponse;
import com.gestione.ristoranti.gestione_ristoranti.staff.dto.StaffResponse;
import com.gestione.ristoranti.gestione_ristoranti.staff.dto.TurnoRequest;
import com.gestione.ristoranti.gestione_ristoranti.staff.dto.TurnoResponse;
import com.gestione.ristoranti.gestione_ristoranti.staff.model.StatoTurno;
import com.gestione.ristoranti.gestione_ristoranti.staff.model.Turno;
import com.gestione.ristoranti.gestione_ristoranti.staff.repository.TurnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StaffService {

    private final UtenteRepository utenteRepository;
    private final RuoloRepository ruoloRepository;
    private final TurnoRepository turnoRepository;
    private final OrdineRepository ordineRepository;
    private final PrenotazioneRepository prenotazioneRepository;

    // ── Staff ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<StaffResponse> getStaff() {
        return utenteRepository.findByRuoloNomeNot("CLIENTE")
                .stream()
                .map(this::mapToStaffResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StaffDetailResponse getStaffById(Long id) {
        Utente utente = trovaUtente(id);
        if ("CLIENTE".equals(utente.getRuolo().getNome())) {
            throw new IllegalStateException("L'utente non è un membro dello staff");
        }
        List<TurnoResponse> turni = turnoRepository.findByUtenteId(id)
                .stream()
                .map(this::mapToTurnoResponse)
                .collect(Collectors.toList());
        return StaffDetailResponse.builder()
                .id(utente.getId())
                .nome(utente.getNome())
                .email(utente.getEmail())
                .ruolo(utente.getRuolo().getNome())
                .turni(turni)
                .build();
    }

    public StaffResponse aggiornaStaff(Long id, AggiornaStaffRequest req) {
        Utente utente = trovaUtente(id);

        if (req.getNome() != null && !req.getNome().isBlank()) {
            utente.setNome(req.getNome());
        }
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            if (!req.getEmail().equals(utente.getEmail()) && utenteRepository.existsByEmail(req.getEmail())) {
                throw new IllegalStateException("Email già in uso: " + req.getEmail());
            }
            utente.setEmail(req.getEmail());
        }
        if (req.getRuolo() != null && !req.getRuolo().isBlank()) {
            Ruolo ruolo = ruoloRepository.findByNome(req.getRuolo().toUpperCase())
                    .orElseThrow(() -> new ResourceNotFoundException("Ruolo non trovato: " + req.getRuolo()));
            utente.setRuolo(ruolo);
        }
        return mapToStaffResponse(utenteRepository.save(utente));
    }

    public void eliminaStaff(Long id) {
        Utente utente = trovaUtente(id);
        String emailCorrente = SecurityContextHolder.getContext().getAuthentication().getName();
        if (utente.getEmail().equals(emailCorrente)) {
            throw new IllegalStateException("Non puoi eliminare il tuo stesso account");
        }
        turnoRepository.deleteAll(turnoRepository.findByUtenteId(id));
        List<Prenotazione> prenotazioni = prenotazioneRepository.findByUtente(utente);
        prenotazioneRepository.deleteAll(prenotazioni);
        List<Ordine> ordini = ordineRepository.findByUtenteId(id);
        ordini.forEach(o -> o.setUtente(null));
        ordineRepository.saveAll(ordini);
        utenteRepository.delete(utente);
    }

    // ── Turni ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TurnoResponse> getTurni() {
        return turnoRepository.findAll()
                .stream()
                .map(this::mapToTurnoResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TurnoResponse> getTurniByUtente(Long utenteId) {
        trovaUtente(utenteId);
        return turnoRepository.findByUtenteId(utenteId)
                .stream()
                .map(this::mapToTurnoResponse)
                .collect(Collectors.toList());
    }

    public TurnoResponse creaTurno(TurnoRequest req) {
        Utente utente = trovaUtente(req.getUtenteId());
        if (req.getDataFine() != null && req.getDataFine().isBefore(req.getDataInizio())) {
            throw new IllegalArgumentException("La data di fine deve essere successiva alla data di inizio");
        }

        Turno turno = new Turno();
        turno.setUtente(utente);
        turno.setDataInizio(req.getDataInizio());
        turno.setDataFine(req.getDataFine());
        turno.setStato(req.getStato() != null ? req.getStato() : StatoTurno.PIANIFICATO);
        turno.setNote(req.getNote());

        return mapToTurnoResponse(turnoRepository.save(turno));
    }

    public TurnoResponse aggiornaTurno(Long id, TurnoRequest req) {
        Turno turno = trovaTurno(id);

        if (req.getUtenteId() != null) {
            turno.setUtente(trovaUtente(req.getUtenteId()));
        }
        if (req.getDataInizio() != null) {
            turno.setDataInizio(req.getDataInizio());
        }
        if (req.getDataFine() != null) {
            if (req.getDataFine().isBefore(turno.getDataInizio())) {
                throw new IllegalArgumentException("La data di fine deve essere successiva alla data di inizio");
            }
            turno.setDataFine(req.getDataFine());
        }
        if (req.getStato() != null) {
            turno.setStato(req.getStato());
        }
        if (req.getNote() != null) {
            turno.setNote(req.getNote());
        }

        return mapToTurnoResponse(turnoRepository.save(turno));
    }

    public void eliminaTurno(Long id) {
        trovaTurno(id);
        turnoRepository.deleteById(id);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Utente trovaUtente(Long id) {
        return utenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato: " + id));
    }

    private Turno trovaTurno(Long id) {
        return turnoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turno non trovato: " + id));
    }

    private StaffResponse mapToStaffResponse(Utente u) {
        return StaffResponse.builder()
                .id(u.getId())
                .nome(u.getNome())
                .email(u.getEmail())
                .ruolo(u.getRuolo().getNome())
                .nrTurni(turnoRepository.countByUtenteId(u.getId()))
                .build();
    }

    private TurnoResponse mapToTurnoResponse(Turno t) {
        return TurnoResponse.builder()
                .id(t.getId())
                .utenteId(t.getUtente().getId())
                .utenteNome(t.getUtente().getNome())
                .dataInizio(t.getDataInizio())
                .dataFine(t.getDataFine())
                .stato(t.getStato())
                .note(t.getNote())
                .build();
    }
}
