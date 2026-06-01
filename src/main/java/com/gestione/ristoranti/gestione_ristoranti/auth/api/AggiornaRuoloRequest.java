package com.gestione.ristoranti.gestione_ristoranti.auth.api;

import jakarta.validation.constraints.NotBlank;

public class AggiornaRuoloRequest {

    @NotBlank(message = "Il ruolo è obbligatorio")
    private String ruolo;

    public AggiornaRuoloRequest() {}

    public AggiornaRuoloRequest(String ruolo) {
        this.ruolo = ruolo;
    }

    public String getRuolo() { return ruolo; }
    public void setRuolo(String ruolo) { this.ruolo = ruolo; }
}
