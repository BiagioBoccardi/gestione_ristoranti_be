package com.gestione.ristoranti.gestione_ristoranti.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class VerificaPrimoAccessoRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String codice;

    public VerificaPrimoAccessoRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCodice() { return codice; }
    public void setCodice(String codice) { this.codice = codice; }
}
