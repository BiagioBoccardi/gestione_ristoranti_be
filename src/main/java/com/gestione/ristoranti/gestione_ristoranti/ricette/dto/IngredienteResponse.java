package com.gestione.ristoranti.gestione_ristoranti.ricette.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class IngredienteResponse {

    private Long id;
    private String nome;
    private String unitaMisura;
    private BigDecimal costoPerUnita;
}
