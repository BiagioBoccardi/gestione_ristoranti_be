package com.gestione.ristoranti.gestione_ristoranti.ordini.service;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.Ordine;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.OrdineItem;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Piatto;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.StatoOrdine;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.StatoTavolo;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Tavolo;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Utente;
import com.gestione.ristoranti.gestione_ristoranti.auth.repository.UtenteRepository;
import com.gestione.ristoranti.gestione_ristoranti.exception.ConflictException;
import com.gestione.ristoranti.gestione_ristoranti.exception.ResourceNotFoundException;
import com.gestione.ristoranti.gestione_ristoranti.menu.repository.PiattoRepository;
import com.gestione.ristoranti.gestione_ristoranti.ordini.dto.CambioStatoRequest;
import com.gestione.ristoranti.gestione_ristoranti.ordini.dto.OrdineItemRequest;
import com.gestione.ristoranti.gestione_ristoranti.ordini.dto.OrdineItemResponse;
import com.gestione.ristoranti.gestione_ristoranti.ordini.dto.OrdineRequest;
import com.gestione.ristoranti.gestione_ristoranti.ordini.dto.OrdineResponse;
import com.gestione.ristoranti.gestione_ristoranti.ordini.dto.OrdineStatoEvent;
import com.gestione.ristoranti.gestione_ristoranti.ordini.dto.OrdineUpdateRequest;
import com.gestione.ristoranti.gestione_ristoranti.ordini.dto.PiattoSummaryResponse;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.OrdineRepository;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.TavoloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrdineService {

    private static final Map<StatoOrdine, StatoOrdine> TRANSIZIONI_VALIDE = Map.of(
            StatoOrdine.IN_ATTESA, StatoOrdine.IN_PREPARAZIONE,
            StatoOrdine.IN_PREPARAZIONE, StatoOrdine.PRONTO,
            StatoOrdine.PRONTO, StatoOrdine.CONSEGNATO
    );

    private final OrdineRepository ordineRepository;
    private final TavoloRepository tavoloRepository;
    private final UtenteRepository utenteRepository;
    private final PiattoRepository piattoRepository;
    private final OrdineEventPublisher ordineEventPublisher;

    /**
     * Restituisce tutti gli ordini, opzionalmente filtrati per stato.
     *
     * @param stato filtro opzionale; se null restituisce tutti gli ordini
     * @return lista di ordini mappati come DTO
     */
    public List<OrdineResponse> getAll(StatoOrdine stato) {
        List<Ordine> ordini = (stato != null)
                ? ordineRepository.findByStatoWithItems(stato)
                : ordineRepository.findAllWithItems();
        return ordini.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrdineResponse> getByTavolo(Long tavoloId) {
        Tavolo tavolo = tavoloRepository.findById(tavoloId)
                .orElseThrow(() -> new ResourceNotFoundException("Tavolo non trovato"));
        return ordineRepository.findByTavoloWithItems(tavolo).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OrdineResponse getById(Long id) {
        return ordineRepository.findByIdWithItems(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Ordine non trovato"));
    }

    /**
     * Crea un nuovo ordine associato a un tavolo e a un utente autenticato.
     * Imposta il tavolo come OCCUPATO e pubblica un evento WebSocket con stato IN_ATTESA.
     *
     * @param request   dati dell'ordine con tavoloId e lista item
     * @param utenteId  id dell'utente cameriere/admin che crea l'ordine
     * @return ordine creato come DTO
     * @throws ConflictException se il tavolo è RISERVATO o un piatto non è disponibile
     */
    @Transactional
    public OrdineResponse create(OrdineRequest request, Long utenteId) {
        Tavolo tavolo = tavoloRepository.findById(request.getTavoloId())
                .orElseThrow(() -> new ResourceNotFoundException("Tavolo non trovato"));

        if (tavolo.getStato() == StatoTavolo.RISERVATO) {
            throw new ConflictException("Il tavolo è riservato e non accetta ordini");
        }

        Utente utente = utenteRepository.findById(utenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));

        Ordine ordine = new Ordine(tavolo, utente, StatoOrdine.IN_ATTESA, LocalDateTime.now());
        ordine = ordineRepository.save(ordine);

        Map<Long, Piatto> piattiMap = buildPiattiMap(request.getItems());
        for (OrdineItemRequest itemReq : request.getItems()) {
            Piatto piatto = piattiMap.get(itemReq.getPiattoId());
            if (piatto == null) throw new ResourceNotFoundException("Piatto non trovato: id=" + itemReq.getPiattoId());
            if (!piatto.getDisponibile()) {
                throw new ConflictException("Il piatto '" + piatto.getNome() + "' non è disponibile");
            }
            OrdineItem item = new OrdineItem(ordine, piatto, itemReq.getQuantita(), piatto.getPrezzo(), itemReq.getNote());
            ordine.getItems().add(item);
        }

        tavolo.setStato(StatoTavolo.OCCUPATO);
        tavoloRepository.save(tavolo);

        Ordine saved = ordineRepository.save(ordine);

        ordineEventPublisher.pubblicaStatoOrdine(new OrdineStatoEvent(
                saved.getId(),
                saved.getTavolo().getId(),
                saved.getTavolo().getNumero(),
                null,
                StatoOrdine.IN_ATTESA,
                LocalDateTime.now()
        ));

        return mapToResponse(saved);
    }

    @Transactional
    public OrdineResponse createAnonimo(OrdineRequest request) {
        Tavolo tavolo = tavoloRepository.findById(request.getTavoloId())
                .orElseThrow(() -> new ResourceNotFoundException("Tavolo non trovato"));

        if (tavolo.getStato() == StatoTavolo.RISERVATO) {
            throw new ConflictException("Il tavolo è riservato e non accetta ordini");
        }

        Ordine ordine = new Ordine(tavolo, null, StatoOrdine.IN_ATTESA, LocalDateTime.now());
        ordine = ordineRepository.save(ordine);

        Map<Long, Piatto> piattiMap = buildPiattiMap(request.getItems());
        for (OrdineItemRequest itemReq : request.getItems()) {
            Piatto piatto = piattiMap.get(itemReq.getPiattoId());
            if (piatto == null) throw new ResourceNotFoundException("Piatto non trovato: id=" + itemReq.getPiattoId());
            if (!piatto.getDisponibile()) {
                throw new ConflictException("Il piatto '" + piatto.getNome() + "' non è disponibile");
            }
            OrdineItem item = new OrdineItem(ordine, piatto, itemReq.getQuantita(), piatto.getPrezzo(), itemReq.getNote());
            ordine.getItems().add(item);
        }

        tavolo.setStato(StatoTavolo.OCCUPATO);
        tavoloRepository.save(tavolo);

        Ordine saved = ordineRepository.save(ordine);

        ordineEventPublisher.pubblicaStatoOrdine(new OrdineStatoEvent(
                saved.getId(),
                saved.getTavolo().getId(),
                saved.getTavolo().getNumero(),
                null,
                StatoOrdine.IN_ATTESA,
                LocalDateTime.now()
        ));

        return mapToResponse(saved);
    }

    @Transactional
    public OrdineResponse update(Long id, OrdineUpdateRequest request) {
        Ordine ordine = ordineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordine non trovato"));

        ordine.getItems().clear();
        Map<Long, Piatto> piattiMap = buildPiattiMap(request.getItems());
        for (OrdineItemRequest itemReq : request.getItems()) {
            Piatto piatto = piattiMap.get(itemReq.getPiattoId());
            if (piatto == null) throw new ResourceNotFoundException("Piatto non trovato: id=" + itemReq.getPiattoId());
            OrdineItem item = new OrdineItem(ordine, piatto, itemReq.getQuantita(), piatto.getPrezzo(), itemReq.getNote());
            ordine.getItems().add(item);
        }

        return mapToResponse(ordineRepository.save(ordine));
    }

    /**
     * Aggiorna lo stato di un ordine seguendo la macchina a stati:
     * IN_ATTESA → IN_PREPARAZIONE → PRONTO → CONSEGNATO.
     * Se lo stato diventa CONSEGNATO e non ci sono altri ordini attivi sul tavolo,
     * il tavolo viene liberato automaticamente.
     * Pubblica un evento WebSocket alla cucina ad ogni transizione.
     *
     * @param id      id dell'ordine
     * @param request nuovo stato desiderato
     * @return ordine aggiornato come DTO
     * @throws IllegalStateException se la transizione di stato non è valida
     */
    @Transactional
    public OrdineResponse aggiornaStato(Long id, CambioStatoRequest request) {
        Ordine ordine = ordineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordine non trovato"));

        StatoOrdine statoCorrente = ordine.getStato();
        StatoOrdine nuovoStato = request.getNuovoStato();

        StatoOrdine prossimoValido = TRANSIZIONI_VALIDE.get(statoCorrente);
        if (prossimoValido == null || !prossimoValido.equals(nuovoStato)) {
            throw new IllegalStateException(
                    "Transizione non valida: " + statoCorrente + " → " + nuovoStato);
        }

        ordine.setStato(nuovoStato);

        if (nuovoStato == StatoOrdine.CONSEGNATO) {
            liberaTavoloSeNecessario(ordine.getTavolo(), id);
        }

        Ordine saved = ordineRepository.save(ordine);

        ordineEventPublisher.pubblicaStatoOrdine(new OrdineStatoEvent(
                saved.getId(),
                saved.getTavolo().getId(),
                saved.getTavolo().getNumero(),
                statoCorrente,
                nuovoStato,
                LocalDateTime.now()
        ));

        return mapToResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Ordine ordine = ordineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordine non trovato"));

        if (ordine.getStato() != StatoOrdine.IN_ATTESA) {
            throw new ConflictException("Solo gli ordini in attesa possono essere eliminati");
        }
        ordineRepository.delete(ordine);
    }

    private Map<Long, Piatto> buildPiattiMap(List<OrdineItemRequest> items) {
        List<Long> ids = items.stream().map(OrdineItemRequest::getPiattoId).collect(Collectors.toList());
        return piattoRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Piatto::getId, p -> p));
    }

    private void liberaTavoloSeNecessario(Tavolo tavolo, Long ordineCorrenteId) {
        boolean altriAttivi = ordineRepository.findByTavolo(tavolo).stream()
                .filter(o -> !o.getId().equals(ordineCorrenteId))
                .anyMatch(o -> o.getStato() != StatoOrdine.CONSEGNATO);
        if (!altriAttivi) {
            tavolo.setStato(StatoTavolo.IN_ATTESA_CONTO);
            tavoloRepository.save(tavolo);
        }
    }

    private OrdineResponse mapToResponse(Ordine ordine) {
        List<OrdineItemResponse> items = ordine.getItems().stream()
                .map(item -> OrdineItemResponse.builder()
                        .id(item.getId())
                        .piatto(PiattoSummaryResponse.builder()
                                .id(item.getPiatto().getId())
                                .nome(item.getPiatto().getNome())
                                .prezzo(item.getPiatto().getPrezzo())
                                .build())
                        .prezzoUnitario(prezzoEffettivo(item))
                        .quantita(item.getQuantita())
                        .note(item.getNote())
                        .build())
                .collect(Collectors.toList());

        BigDecimal totale = ordine.getItems().stream()
                .map(i -> prezzoEffettivo(i).multiply(BigDecimal.valueOf(i.getQuantita())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return OrdineResponse.builder()
                .id(ordine.getId())
                .tavoloId(ordine.getTavolo().getId())
                .numeroTavolo(ordine.getTavolo().getNumero())
                .utenteId(ordine.getUtente() != null ? ordine.getUtente().getId() : null)
                .nomeUtente(ordine.getUtente() != null ? ordine.getUtente().getNome() : "Cliente QR")
                .stato(ordine.getStato())
                .items(items)
                .totale(totale)
                .creatoAt(ordine.getCreatoIl())
                .build();
    }

    private BigDecimal prezzoEffettivo(OrdineItem item) {
        return item.getPrezzoUnitario() != null
                ? item.getPrezzoUnitario()
                : item.getPiatto().getPrezzo();
    }
}
