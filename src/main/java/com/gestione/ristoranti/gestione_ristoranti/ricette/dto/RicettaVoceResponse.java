package com.gestione.ristoranti.gestione_ristoranti.ricette.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class RicettaVoceResponse {

    private Long id;
    private Long ingredienteId;
    private String nomeIngrediente;
    private String unitaMisura;
    private BigDecimal quantita;
    private BigDecimal percentualeScarto;
    private BigDecimal costoVoce;
}
