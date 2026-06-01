package com.gestione.ristoranti.gestione_ristoranti.menu.service;

import com.gestione.ristoranti.gestione_ristoranti.exception.ResourceNotFoundException;
import com.gestione.ristoranti.gestione_ristoranti.menu.dto.PiattoRequest;
import com.gestione.ristoranti.gestione_ristoranti.menu.dto.PiattoResponse;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Categoria;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Piatto;
import com.gestione.ristoranti.gestione_ristoranti.menu.repository.CategoriaRepository;
import com.gestione.ristoranti.gestione_ristoranti.menu.repository.PiattoRepository;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.OrdineItemRepository;
import com.gestione.ristoranti.gestione_ristoranti.ricette.repository.RicettaVoceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PiattoService {

    private final PiattoRepository piattoRepository;
    private final CategoriaRepository categoriaRepository;
    private final RicettaVoceRepository ricettaVoceRepository;
    private final OrdineItemRepository ordineItemRepository;

    @Cacheable(value = "piatti", key = "#categoriaId + '-' + #disponibile")
    public List<PiattoResponse> getAll(Long categoriaId, Boolean disponibile) {
        List<Piatto> piatti;

        if (categoriaId != null && disponibile != null) {
            Categoria cat = categoriaRepository.findById(categoriaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria non trovata"));
            piatti = piattoRepository.findByCategoriaAndDisponibile(cat, disponibile);
        } else if (categoriaId != null) {
            Categoria cat = categoriaRepository.findById(categoriaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria non trovata"));
            piatti = piattoRepository.findByCategoria(cat);
        } else if (disponibile != null) {
            piatti = piattoRepository.findByDisponibile(disponibile);
        } else {
            piatti = piattoRepository.findAll();
        }

        return piatti.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public PiattoResponse getById(Long id) {
        return piattoRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Piatto non trovato"));
    }

    @CacheEvict(value = "piatti", allEntries = true)
    @Transactional
    public PiattoResponse create(PiattoRequest request) {
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria non trovata"));

        Piatto piatto = new Piatto();
        updatePiattoFields(piatto, request, categoria);

        return mapToResponse(piattoRepository.save(piatto));
    }

    @CacheEvict(value = "piatti", allEntries = true)
    @Transactional
    public PiattoResponse update(Long id, PiattoRequest request) {
        Piatto piatto = piattoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Piatto non trovato"));
        
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria non trovata"));

        updatePiattoFields(piatto, request, categoria);
        return mapToResponse(piattoRepository.save(piatto));
    }

    @CacheEvict(value = "piatti", allEntries = true)
    @Transactional
    public void toggleDisponibilita(Long id) {
        Piatto piatto = piattoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Piatto non trovato"));
        piatto.setDisponibile(!piatto.getDisponibile());
        piattoRepository.save(piatto);
    }

    @CacheEvict(value = "piatti", allEntries = true)
    @Transactional
    public void delete(Long id) {
        if (!piattoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Piatto non trovato");
        }
        ordineItemRepository.deleteByPiattoId(id);
        ricettaVoceRepository.deleteByPiattoId(id);
        piattoRepository.deleteById(id);
    }

    // Helper per aggiornare i campi ed evitare duplicazione di codice tra create e update
    private void updatePiattoFields(Piatto piatto, PiattoRequest request, Categoria categoria) {
        piatto.setNome(request.getNome());
        piatto.setDescrizione(request.getDescrizione());
        piatto.setPrezzo(request.getPrezzo());
        piatto.setDisponibile(request.getDisponibile());
        piatto.setFoto(request.getImmagineUrl());
        piatto.setCategoria(categoria);
    }

    private PiattoResponse mapToResponse(Piatto piatto) {
        return PiattoResponse.builder()
                .id(piatto.getId())
                .nome(piatto.getNome())
                .descrizione(piatto.getDescrizione())
                .prezzo(piatto.getPrezzo())
                .disponibile(piatto.getDisponibile())
                .immagineUrl(piatto.getFoto())
                .categoria(PiattoResponse.CategoriaInfo.builder()
                        .id(piatto.getCategoria().getId())
                        .nome(piatto.getCategoria().getNome())
                        .build())
                .build();
    }
}
