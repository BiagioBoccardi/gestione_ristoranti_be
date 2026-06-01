package com.gestione.ristoranti.gestione_ristoranti.conti.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class SplitBillResponse {

    private BigDecimal totale;
    private Integer nPersone;
    private BigDecimal quotaPerPersona;
}
