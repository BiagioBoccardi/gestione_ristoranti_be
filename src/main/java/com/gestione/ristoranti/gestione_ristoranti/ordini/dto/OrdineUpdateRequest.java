package com.gestione.ristoranti.gestione_ristoranti.ordini.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrdineUpdateRequest {

    @NotEmpty(message = "L'ordine deve contenere almeno un piatto")
    @Valid
    private List<OrdineItemRequest> items;
}