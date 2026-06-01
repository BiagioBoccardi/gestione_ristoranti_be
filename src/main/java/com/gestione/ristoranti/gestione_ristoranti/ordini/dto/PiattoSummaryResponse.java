package com.gestione.ristoranti.gestione_ristoranti.ordini.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PiattoSummaryResponse {

    private Long id;
    private String nome;
    private BigDecimal prezzo;
}