package com.gestione.ristoranti.gestione_ristoranti.ordini.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.StatoOrdine;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CambioStatoRequest {

    @NotNull(message = "Il nuovo stato è obbligatorio")
    @JsonProperty("nuovoStato")
    private StatoOrdine nuovoStato;
}
