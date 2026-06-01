package com.gestione.ristoranti.gestione_ristoranti.auth.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @Column(nullable = false)
    private LocalDateTime scadenza;

    @Column(nullable = false)
    private boolean usato = false;

    public PasswordResetToken() {}

    public PasswordResetToken(String token, Utente utente, LocalDateTime scadenza) {
        this.token = token;
        this.utente = utente;
        this.scadenza = scadenza;
        this.usato = false;
    }

    public Long getId() { return id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Utente getUtente() { return utente; }
    public void setUtente(Utente utente) { this.utente = utente; }

    public LocalDateTime getScadenza() { return scadenza; }
    public void setScadenza(LocalDateTime scadenza) { this.scadenza = scadenza; }

    public boolean isUsato() { return usato; }
    public void setUsato(boolean usato) { this.usato = usato; }
}
