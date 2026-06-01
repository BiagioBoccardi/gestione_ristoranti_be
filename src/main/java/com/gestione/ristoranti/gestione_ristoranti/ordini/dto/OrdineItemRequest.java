package com.gestione.ristoranti.gestione_ristoranti.ordini.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrdineItemRequest {

    @NotNull(message = "Il piatto è obbligatorio")
    private Long piattoId;

    @NotNull(message = "La quantità è obbligatoria")
    @Min(value = 1, message = "La quantità deve essere almeno 1")
    private Integer quantita;

    private String note;
}
