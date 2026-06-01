package com.gestione.ristoranti.gestione_ristoranti.menu.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PiattoResponse {
    private Long id;
    private String nome;
    private String descrizione;
    private BigDecimal prezzo;
    private boolean disponibile;
    private String immagineUrl;
    private CategoriaInfo categoria;

    @Data
    @Builder
    public static class CategoriaInfo {
        private Long id;
        private String nome;
    }
}