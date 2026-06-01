package com.gestione.ristoranti.gestione_ristoranti.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordRequest {

    @Email(message = "Inserisci un'email valida")
    @NotBlank(message = "L'email è obbligatoria")
    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
