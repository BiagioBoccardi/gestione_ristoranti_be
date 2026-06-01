package com.gestione.ristoranti.gestione_ristoranti.ordini.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class OrdineItemResponse {

    private Long id;
    private PiattoSummaryResponse piatto;
    private BigDecimal prezzoUnitario;
    private Integer quantita;
    private String note;
}
