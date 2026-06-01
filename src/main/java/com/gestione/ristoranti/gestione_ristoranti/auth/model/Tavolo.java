package com.gestione.ristoranti.gestione_ristoranti.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tavoli", indexes = {
    @Index(name = "idx_tavolo_stato", columnList = "stato")
})
public class Tavolo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer numero;

    @Column(nullable = false)
    private Integer coperti;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoTavolo stato;

    @Column(unique = true)
    private String qrToken;

    @OneToMany(mappedBy = "tavolo", fetch = FetchType.LAZY)
    private List<Prenotazione> prenotazioni = new ArrayList<>();

    @OneToMany(mappedBy = "tavolo", fetch = FetchType.LAZY)
    private List<Ordine> ordini = new ArrayList<>();

    public Tavolo() {
    }

    public Tavolo(Integer numero, Integer coperti, StatoTavolo stato) {
        this.numero = numero;
        this.coperti = coperti;
        this.stato = stato;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Integer getCoperti() {
        return coperti;
    }

    public void setCoperti(Integer coperti) {
        this.coperti = coperti;
    }

    public StatoTavolo getStato() {
        return stato;
    }

    public void setStato(StatoTavolo stato) {
        this.stato = stato;
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

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }
}
