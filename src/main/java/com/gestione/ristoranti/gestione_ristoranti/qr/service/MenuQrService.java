package com.gestione.ristoranti.gestione_ristoranti.qr.service;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.Categoria;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Piatto;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Tavolo;
import com.gestione.ristoranti.gestione_ristoranti.exception.ResourceNotFoundException;
import com.gestione.ristoranti.gestione_ristoranti.menu.repository.CategoriaRepository;
import com.gestione.ristoranti.gestione_ristoranti.menu.repository.PiattoRepository;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.TavoloRepository;
import com.gestione.ristoranti.gestione_ristoranti.qr.dto.MenuQrResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuQrService {

    private final TavoloRepository tavoloRepository;
    private final CategoriaRepository categoriaRepository;
    private final PiattoRepository piattoRepository;

    public MenuQrResponse getMenuByToken(String token) {
        Tavolo tavolo = tavoloRepository.findByQrToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("QR code non valido"));

        List<Categoria> categorie = categoriaRepository.findAll();

        List<MenuQrResponse.CategoriaMenuResponse> categorieConPiatti = categorie.stream()
                .map(cat -> {
                    List<Piatto> piatti = piattoRepository.findByCategoriaAndDisponibile(cat, true);
                    List<MenuQrResponse.PiattoMenuResponse> piattiDto = piatti.stream()
                            .map(p -> MenuQrResponse.PiattoMenuResponse.builder()
                                    .id(p.getId())
                                    .nome(p.getNome())
                                    .descrizione(p.getDescrizione())
                                    .prezzo(p.getPrezzo())
                                    .foto(p.getFoto())
                                    .build())
                            .collect(Collectors.toList());
                    return MenuQrResponse.CategoriaMenuResponse.builder()
                            .id(cat.getId())
                            .nome(cat.getNome())
                            .descrizione(cat.getDescrizione())
                            .piatti(piattiDto)
                            .build();
                })
                .filter(c -> !c.getPiatti().isEmpty())
                .collect(Collectors.toList());

        return MenuQrResponse.builder()
                .tavoloId(tavolo.getId())
                .numeroTavolo(tavolo.getNumero())
                .categorie(categorieConPiatti)
                .build();
    }
}