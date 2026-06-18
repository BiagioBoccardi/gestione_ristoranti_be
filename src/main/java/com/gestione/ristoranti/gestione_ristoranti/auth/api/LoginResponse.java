package com.gestione.ristoranti.gestione_ristoranti.auth.api;

public class LoginResponse {

    private String token;
    private String type = "Bearer";
    private String nome;
    private String ruolo;
    private Boolean primoAccesso;
    private String email;

    public LoginResponse(String token, String nome, String ruolo) {
        this.token = token;
        this.nome = nome;
        this.ruolo = ruolo;
    }

    private LoginResponse() {}

    public static LoginResponse forPrimoAccesso(String email) {
        LoginResponse r = new LoginResponse();
        r.primoAccesso = true;
        r.email = email;
        return r;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getType() { return type; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getRuolo() { return ruolo; }
    public void setRuolo(String ruolo) { this.ruolo = ruolo; }

    public Boolean getPrimoAccesso() { return primoAccesso; }

    public String getEmail() { return email; }
}
