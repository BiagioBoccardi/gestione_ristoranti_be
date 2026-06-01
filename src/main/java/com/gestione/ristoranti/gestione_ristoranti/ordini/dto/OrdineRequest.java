package com.gestione.ristoranti.gestione_ristoranti.ordini.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrdineRequest {

    @NotNull(message = "Il tavolo è obbligatorio")
    private Long tavoloId;

    @NotEmpty(message = "L'ordine deve contenere almeno un piatto")
    @Valid
    private List<OrdineItemRequest> items;
}
