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
import java.math.BigDecimal;

@Entity
@Table(name = "ordine_items", indexes = {
    @Index(name = "idx_ordine_item_ordine", columnList = "ordine_id"),
    @Index(name = "idx_ordine_item_piatto", columnList = "piatto_id")
})
public class OrdineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ordine_id", nullable = false)
    private Ordine ordine;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "piatto_id", nullable = false)
    private Piatto piatto;

    @Column(nullable = false)
    private Integer quantita;

    @Column(precision = 10, scale = 2)
    private BigDecimal prezzoUnitario;

    @Column(columnDefinition = "TEXT")
    private String note;

    public OrdineItem() {
    }

    public OrdineItem(Ordine ordine, Piatto piatto, Integer quantita, BigDecimal prezzoUnitario, String note) {
        this.ordine = ordine;
        this.piatto = piatto;
        this.quantita = quantita;
        this.prezzoUnitario = prezzoUnitario;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ordine getOrdine() {
        return ordine;
    }

    public void setOrdine(Ordine ordine) {
        this.ordine = ordine;
    }

    public Piatto getPiatto() {
        return piatto;
    }

    public void setPiatto(Piatto piatto) {
        this.piatto = piatto;
    }

    public Integer getQuantita() {
        return quantita;
    }

    public void setQuantita(Integer quantita) {
        this.quantita = quantita;
    }

    public BigDecimal getPrezzoUnitario() {
        return prezzoUnitario;
    }

    public void setPrezzoUnitario(BigDecimal prezzoUnitario) {
        this.prezzoUnitario = prezzoUnitario;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
