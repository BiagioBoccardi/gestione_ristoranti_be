package com.gestione.ristoranti.gestione_ristoranti.ricette.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class RicettaResponse {

    private Long piattoId;
    private String nomePiatto;
    private BigDecimal prezzoVendita;
    private List<RicettaVoceResponse> voci;
    private BigDecimal costoTotale;
    private BigDecimal foodCostPercentuale;
}
