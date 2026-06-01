package com.gestione.ristoranti.gestione_ristoranti.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoriaResponse {
    private Long id;
    private String nome;
    private String descrizione;
}