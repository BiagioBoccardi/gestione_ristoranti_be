package com.gestione.ristoranti.gestione_ristoranti.ricette.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class RicettaVoceRequest {

    @NotNull(message = "L'ingrediente è obbligatorio")
    private Long ingredienteId;

    @NotNull(message = "La quantità è obbligatoria")
    @DecimalMin(value = "0.0001", message = "La quantità deve essere maggiore di zero")
    private BigDecimal quantita;

    @NotNull(message = "La percentuale di scarto è obbligatoria")
    @DecimalMin(value = "0.00", message = "La percentuale di scarto non può essere negativa")
    private BigDecimal percentualeScarto;
}
