package com.gestione.ristoranti.gestione_ristoranti.auth.api;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.Utente;

public class UtenteResponse {

    private Long id;
    private String nome;
    private String email;
    private String ruolo;

    public UtenteResponse() {}

    public UtenteResponse(Long id, String nome, String email, String ruolo) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.ruolo = ruolo;
    }

    public static UtenteResponse from(Utente utente) {
        return new UtenteResponse(
                utente.getId(),
                utente.getNome(),
                utente.getEmail(),
                utente.getRuolo().getNome()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRuolo() { return ruolo; }
    public void setRuolo(String ruolo) { this.ruolo = ruolo; }
}
