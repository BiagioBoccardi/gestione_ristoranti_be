package com.gestione.ristoranti.gestione_ristoranti.ricette.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class IngredienteRequest {

    @NotBlank(message = "Il nome è obbligatorio")
    private String nome;

    @NotBlank(message = "L'unità di misura è obbligatoria")
    private String unitaMisura;

    @NotNull(message = "Il costo per unità è obbligatorio")
    @DecimalMin(value = "0.0001", message = "Il costo deve essere maggiore di zero")
    private BigDecimal costoPerUnita;
}
