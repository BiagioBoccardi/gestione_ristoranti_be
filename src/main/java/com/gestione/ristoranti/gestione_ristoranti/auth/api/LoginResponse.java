package com.gestione.ristoranti.gestione_ristoranti.auth.api;

public class LoginResponse {

    private String token;
    private String type = "Bearer";
    private String nome;
    private String ruolo;

    public LoginResponse(String token, String nome, String ruolo) {
        this.token = token;
        this.nome = nome;
        this.ruolo = ruolo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getRuolo() {
        return ruolo;
    }

    public void setRuolo(String ruolo) {
        this.ruolo = ruolo;
    }
}
