package com.gestione.ristoranti.gestione_ristoranti.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class RegisterRequest {

    @NotBlank
    private String nome;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    public RegisterRequest() {
    }

    public RegisterRequest(String nome, String email, String password) {
        this.nome = nome;
        this.email = email;
        this.password = password;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
