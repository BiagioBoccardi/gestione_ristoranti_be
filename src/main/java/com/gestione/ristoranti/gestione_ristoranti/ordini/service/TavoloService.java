package com.gestione.ristoranti.gestione_ristoranti.ordini.service;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.StatoOrdine;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.StatoTavolo;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Tavolo;
import com.gestione.ristoranti.gestione_ristoranti.exception.ConflictException;
import com.gestione.ristoranti.gestione_ristoranti.exception.ResourceNotFoundException;
import com.gestione.ristoranti.gestione_ristoranti.ordini.dto.TavoloRequest;
import com.gestione.ristoranti.gestione_ristoranti.ordini.dto.TavoloResponse;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.OrdineRepository;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.TavoloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TavoloService {

    private final TavoloRepository tavoloRepository;
    private final OrdineRepository ordineRepository;

    public List<TavoloResponse> getAll(StatoTavolo stato) {
        List<Tavolo> tavoli = (stato != null)
                ? tavoloRepository.findByStato(stato)
                : tavoloRepository.findAll();
        return tavoli.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public TavoloResponse getById(Long id) {
        return tavoloRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Tavolo non trovato"));
    }

    @Transactional
    public TavoloResponse create(TavoloRequest request) {
        if (tavoloRepository.existsByNumero(request.getNumero())) {
            throw new ConflictException("Esiste già un tavolo con numero " + request.getNumero());
        }
        Tavolo tavolo = new Tavolo(request.getNumero(), request.getCoperti(), request.getStato());
        return mapToResponse(tavoloRepository.save(tavolo));
    }

    @Transactional
    public TavoloResponse update(Long id, TavoloRequest request) {
        Tavolo tavolo = tavoloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tavolo non trovato"));

        tavoloRepository.findByNumero(request.getNumero()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ConflictException("Esiste già un tavolo con numero " + request.getNumero());
            }
        });

        tavolo.setNumero(request.getNumero());
        tavolo.setCoperti(request.getCoperti());
        tavolo.setStato(request.getStato());
        return mapToResponse(tavoloRepository.save(tavolo));
    }

    @Transactional
    public TavoloResponse aggiornaStato(Long id, StatoTavolo nuovoStato) {
        Tavolo tavolo = tavoloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tavolo non trovato"));
        tavolo.setStato(nuovoStato);
        return mapToResponse(tavoloRepository.save(tavolo));
    }

    @Transactional
    public void delete(Long id) {
        Tavolo tavolo = tavoloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tavolo non trovato"));

        boolean haOrdiniAttivi = ordineRepository.findByTavolo(tavolo).stream()
                .anyMatch(o -> o.getStato() != StatoOrdine.CONSEGNATO);
        if (haOrdiniAttivi) {
            throw new ConflictException("Impossibile eliminare: il tavolo ha ordini attivi");
        }
        tavoloRepository.delete(tavolo);
    }

    private TavoloResponse mapToResponse(Tavolo tavolo) {
        return TavoloResponse.builder()
                .id(tavolo.getId())
                .numero(tavolo.getNumero())
                .coperti(tavolo.getCoperti())
                .stato(tavolo.getStato())
                .qrToken(tavolo.getQrToken())
                .build();
    }
}
