package com.gestione.ristoranti.gestione_ristoranti.analytics.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class TopPiattoResponse {

    private Long piattoId;
    private String nome;
    private Long quantitaVenduta;
    private BigDecimal revenueGenerata;
}
