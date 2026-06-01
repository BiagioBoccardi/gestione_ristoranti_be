package com.gestione.ristoranti.gestione_ristoranti.qr.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class MenuQrResponse {

    private Long tavoloId;
    private Integer numeroTavolo;
    private List<CategoriaMenuResponse> categorie;

    @Data
    @Builder
    public static class CategoriaMenuResponse {
        private Long id;
        private String nome;
        private String descrizione;
        private List<PiattoMenuResponse> piatti;
    }

    @Data
    @Builder
    public static class PiattoMenuResponse {
        private Long id;
        private String nome;
        private String descrizione;
        private BigDecimal prezzo;
        private String foto;
    }
}