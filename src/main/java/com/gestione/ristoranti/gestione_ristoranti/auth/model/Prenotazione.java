package com.gestione.ristoranti.gestione_ristoranti.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "prenotazioni", indexes = {
    @Index(name = "idx_prenotazioni_data", columnList = "data"),
    @Index(name = "idx_prenotazioni_tavolo_data", columnList = "tavolo_id, data")
})
public class Prenotazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "tavolo_id", nullable = false)
    private Tavolo tavolo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private LocalTime ora;

    @Column(nullable = false)
    private Integer coperti;

    @Column(columnDefinition = "TEXT")
    private String note;

    public Prenotazione() {
    }

    public Prenotazione(Tavolo tavolo, Utente utente, LocalDate data, LocalTime ora, Integer coperti, String note) {
        this.tavolo = tavolo;
        this.utente = utente;
        this.data = data;
        this.ora = ora;
        this.coperti = coperti;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tavolo getTavolo() {
        return tavolo;
    }

    public void setTavolo(Tavolo tavolo) {
        this.tavolo = tavolo;
    }

    public Utente getUtente() {
        return utente;
    }

    public void setUtente(Utente utente) {
        this.utente = utente;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getOra() {
        return ora;
    }

    public void setOra(LocalTime ora) {
        this.ora = ora;
    }

    public Integer getCoperti() {
        return coperti;
    }

    public void setCoperti(Integer coperti) {
        this.coperti = coperti;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
