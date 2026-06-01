package com.gestione.ristoranti.gestione_ristoranti.analytics.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class KpiResponse {

    private BigDecimal revenueTotale;
    private Long ordiniCompletati;
    private BigDecimal valoremedioOrdine;
    private Double copertiMediPrenotazione;
    private BigDecimal foodCostMedioPercentuale;
}
