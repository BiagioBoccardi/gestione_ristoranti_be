package com.gestione.ristoranti.gestione_ristoranti.menu.service;

import com.gestione.ristoranti.gestione_ristoranti.exception.ConflictException;
import com.gestione.ristoranti.gestione_ristoranti.exception.ResourceNotFoundException;
import com.gestione.ristoranti.gestione_ristoranti.menu.dto.CategoriaRequest;
import com.gestione.ristoranti.gestione_ristoranti.menu.dto.CategoriaResponse;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Categoria;
import com.gestione.ristoranti.gestione_ristoranti.menu.repository.CategoriaRepository;
import com.gestione.ristoranti.gestione_ristoranti.menu.repository.PiattoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final PiattoRepository piattoRepository;

    @Cacheable(value = "categorie", key = "'all'")
    public List<CategoriaResponse> getAll() {
        return categoriaRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CategoriaResponse getById(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria non trovata con id: " + id));
        return mapToResponse(categoria);
    }

    @CacheEvict(value = "categorie", allEntries = true)
    @Transactional
    public CategoriaResponse create(CategoriaRequest request) {
        if (categoriaRepository.findByNome(request.getNome()).isPresent()) {
            throw new ConflictException("Esiste già una categoria con questo nome");
        }
        Categoria categoria = new Categoria();
        categoria.setNome(request.getNome());
        categoria.setDescrizione(request.getDescrizione());
        
        return mapToResponse(categoriaRepository.save(categoria));
    }

    @CacheEvict(value = "categorie", allEntries = true)
    @Transactional
    public CategoriaResponse update(Long id, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria non trovata"));
        
        // Verifica se il nuovo nome è già usato da un'altra categoria
        categoriaRepository.findByNome(request.getNome())
                .ifPresent(c -> {
                    if (!c.getId().equals(id)) throw new ConflictException("Il nome è già occupato");
                });

        categoria.setNome(request.getNome());
        categoria.setDescrizione(request.getDescrizione());
        
        return mapToResponse(categoriaRepository.save(categoria));
    }

    @CacheEvict(value = "categorie", allEntries = true)
    @Transactional
    public void delete(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoria non trovata");
        }
        if (piattoRepository.existsByCategoriaId(id)) {
            throw new ConflictException("Impossibile eliminare: ci sono piatti associati a questa categoria");
        }
        categoriaRepository.deleteById(id);
    }

    // Mapping manuale Entity -> DTO
    private CategoriaResponse mapToResponse(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescrizione()
        );
    }
}
