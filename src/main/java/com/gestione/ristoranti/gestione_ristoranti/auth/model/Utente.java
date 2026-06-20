package com.gestione.ristoranti.gestione_ristoranti.auth.model;

import com.gestione.ristoranti.gestione_ristoranti.staff.model.Turno;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "utenti", indexes = {
    @Index(name = "idx_utente_email", columnList = "email")
})
public class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nome;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Column(name = "primo_accesso")
    private Boolean primoAccesso = false;

    @Column(name = "codice_verifica")
    private String codiceVerifica;

    @Column(name = "scadenza_codice")
    private LocalDateTime scadenzaCodice;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ruolo_id", nullable = false)
    private Ruolo ruolo;

    @OneToMany(mappedBy = "utente", fetch = FetchType.LAZY)
    private List<Prenotazione> prenotazioni = new ArrayList<>();

    @OneToMany(mappedBy = "utente", fetch = FetchType.LAZY)
    private List<Ordine> ordini = new ArrayList<>();

    @OneToMany(mappedBy = "utente", fetch = FetchType.LAZY)
    private List<Turno> turni = new ArrayList<>();

    public Utente() {
    }

    public Utente(String nome, String email, String password, Ruolo ruolo) {
        this.nome = nome;
        this.email = email;
        this.password = password;
        this.ruolo = ruolo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Ruolo getRuolo() {
        return ruolo;
    }

    public void setRuolo(Ruolo ruolo) {
        this.ruolo = ruolo;
    }

    public List<Prenotazione> getPrenotazioni() {
        return prenotazioni;
    }

    public void setPrenotazioni(List<Prenotazione> prenotazioni) {
        this.prenotazioni = prenotazioni;
    }

    public List<Ordine> getOrdini() {
        return ordini;
    }

    public void setOrdini(List<Ordine> ordini) {
        this.ordini = ordini;
    }

    public List<Turno> getTurni() {
        return turni;
    }

    public void setTurni(List<Turno> turni) {
        this.turni = turni;
    }

    public boolean isPrimoAccesso() {
        return Boolean.TRUE.equals(primoAccesso);
    }

    public void setPrimoAccesso(boolean primoAccesso) {
        this.primoAccesso = primoAccesso;
    }

    public String getCodiceVerifica() {
        return codiceVerifica;
    }

    public void setCodiceVerifica(String codiceVerifica) {
        this.codiceVerifica = codiceVerifica;
    }

    public LocalDateTime getScadenzaCodice() {
        return scadenzaCodice;
    }

    public void setScadenzaCodice(LocalDateTime scadenzaCodice) {
        this.scadenzaCodice = scadenzaCodice;
    }
}
