package com.gestione.ristoranti.gestione_ristoranti.ricette.service;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.Piatto;
import com.gestione.ristoranti.gestione_ristoranti.exception.ConflictException;
import com.gestione.ristoranti.gestione_ristoranti.exception.ResourceNotFoundException;
import com.gestione.ristoranti.gestione_ristoranti.menu.repository.PiattoRepository;
import com.gestione.ristoranti.gestione_ristoranti.ricette.dto.IngredienteRequest;
import com.gestione.ristoranti.gestione_ristoranti.ricette.dto.IngredienteResponse;
import com.gestione.ristoranti.gestione_ristoranti.ricette.dto.RicettaResponse;
import com.gestione.ristoranti.gestione_ristoranti.ricette.dto.RicettaVoceRequest;
import com.gestione.ristoranti.gestione_ristoranti.ricette.dto.RicettaVoceResponse;
import com.gestione.ristoranti.gestione_ristoranti.ricette.model.Ingrediente;
import com.gestione.ristoranti.gestione_ristoranti.ricette.model.RicettaVoce;
import com.gestione.ristoranti.gestione_ristoranti.ricette.repository.IngredienteRepository;
import com.gestione.ristoranti.gestione_ristoranti.ricette.repository.RicettaVoceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RicetteService {

    private final IngredienteRepository ingredienteRepository;
    private final RicettaVoceRepository ricettaVoceRepository;
    private final PiattoRepository piattoRepository;

    // ── Ingredienti ──────────────────────────────────────────────────────────

    public List<IngredienteResponse> getAllIngredienti() {
        return ingredienteRepository.findAllByOrderByNomeAsc()
                .stream().map(this::mapIngrediente).collect(Collectors.toList());
    }

    public IngredienteResponse creaIngrediente(IngredienteRequest req) {
        if (ingredienteRepository.existsByNomeIgnoreCase(req.getNome())) {
            throw new ConflictException("Ingrediente '" + req.getNome() + "' già esistente");
        }
        Ingrediente ing = new Ingrediente(req.getNome(), req.getUnitaMisura(), req.getCostoPerUnita());
        return mapIngrediente(ingredienteRepository.save(ing));
    }

    public IngredienteResponse aggiornaIngrediente(Long id, IngredienteRequest req) {
        Ingrediente ing = ingredienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingrediente non trovato"));
        ing.setNome(req.getNome());
        ing.setUnitaMisura(req.getUnitaMisura());
        ing.setCostoPerUnita(req.getCostoPerUnita());
        return mapIngrediente(ingredienteRepository.save(ing));
    }

    @Transactional
    public void eliminaIngrediente(Long id) {
        if (!ingredienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ingrediente non trovato");
        }
        ingredienteRepository.deleteById(id);
    }

    // ── Ricette ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public RicettaResponse getRicetta(Long piattoId) {
        Piatto piatto = piattoRepository.findById(piattoId)
                .orElseThrow(() -> new ResourceNotFoundException("Piatto non trovato"));
        List<RicettaVoce> voci = ricettaVoceRepository.findByPiattoId(piattoId);
        return buildRicettaResponse(piatto, voci);
    }

    @Transactional
    public RicettaResponse aggiungiVoce(Long piattoId, RicettaVoceRequest req) {
        Piatto piatto = piattoRepository.findById(piattoId)
                .orElseThrow(() -> new ResourceNotFoundException("Piatto non trovato"));
        Ingrediente ing = ingredienteRepository.findById(req.getIngredienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Ingrediente non trovato"));

        RicettaVoce voce = new RicettaVoce(piatto, ing, req.getQuantita(), req.getPercentualeScarto());
        ricettaVoceRepository.save(voce);
        return buildRicettaResponse(piatto, ricettaVoceRepository.findByPiattoId(piattoId));
    }

    @Transactional
    public void eliminaVoce(Long piattoId, Long voceId) {
        RicettaVoce voce = ricettaVoceRepository.findById(voceId)
                .orElseThrow(() -> new ResourceNotFoundException("Voce ricetta non trovata"));
        if (!voce.getPiatto().getId().equals(piattoId)) {
            throw new ConflictException("La voce non appartiene a questo piatto");
        }
        ricettaVoceRepository.deleteById(voceId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public BigDecimal calcolaCostoPorzione(Long piattoId) {
        return ricettaVoceRepository.findByPiattoId(piattoId).stream()
                .map(this::costoVoce)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    BigDecimal costoVoce(RicettaVoce v) {
        BigDecimal moltiplicatore = BigDecimal.ONE
                .add(v.getPercentualeScarto().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
        return v.getIngrediente().getCostoPerUnita()
                .multiply(v.getQuantita())
                .multiply(moltiplicatore)
                .setScale(4, RoundingMode.HALF_UP);
    }

    private RicettaResponse buildRicettaResponse(Piatto piatto, List<RicettaVoce> voci) {
        List<RicettaVoceResponse> voceResponses = voci.stream()
                .map(v -> RicettaVoceResponse.builder()
                        .id(v.getId())
                        .ingredienteId(v.getIngrediente().getId())
                        .nomeIngrediente(v.getIngrediente().getNome())
                        .unitaMisura(v.getIngrediente().getUnitaMisura())
                        .quantita(v.getQuantita())
                        .percentualeScarto(v.getPercentualeScarto())
                        .costoVoce(costoVoce(v))
                        .build())
                .collect(Collectors.toList());

        BigDecimal costo = voceResponses.stream()
                .map(RicettaVoceResponse::getCostoVoce)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal foodCost = piatto.getPrezzo().compareTo(BigDecimal.ZERO) > 0
                ? costo.divide(piatto.getPrezzo(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return RicettaResponse.builder()
                .piattoId(piatto.getId())
                .nomePiatto(piatto.getNome())
                .prezzoVendita(piatto.getPrezzo())
                .voci(voceResponses)
                .costoTotale(costo.setScale(2, RoundingMode.HALF_UP))
                .foodCostPercentuale(foodCost)
                .build();
    }

    private IngredienteResponse mapIngrediente(Ingrediente ing) {
        return IngredienteResponse.builder()
                .id(ing.getId())
                .nome(ing.getNome())
                .unitaMisura(ing.getUnitaMisura())
                .costoPerUnita(ing.getCostoPerUnita())
                .build();
    }
}
