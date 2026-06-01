package com.gestione.ristoranti.gestione_ristoranti.analytics.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class RevenuePointResponse {

    private String etichetta;
    private BigDecimal revenue;
    private Long ordini;
}
