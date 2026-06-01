package com.gestione.ristoranti.gestione_ristoranti.auth.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {

    @NotBlank(message = "Il token è obbligatorio")
    private String token;

    @NotBlank(message = "La nuova password è obbligatoria")
    @Size(min = 6, message = "La password deve avere almeno 6 caratteri")
    private String nuovaPassword;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getNuovaPassword() { return nuovaPassword; }
    public void setNuovaPassword(String nuovaPassword) { this.nuovaPassword = nuovaPassword; }
}
