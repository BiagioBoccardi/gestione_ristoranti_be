package com.gestione.ristoranti.gestione_ristoranti.analytics.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class FoodCostResponse {

    private Long piattoId;
    private String nomePiatto;
    private BigDecimal prezzoVendita;
    private BigDecimal costoPorzione;
    private BigDecimal foodCostPercentuale;
    private String giudizio; // OTTIMO / BUONO / ATTENZIONE / CRITICO
}
