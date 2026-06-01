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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "conti", indexes = {
    @Index(name = "idx_conto_ordine", columnList = "ordine_id")
})
public class Conto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ordine_id", nullable = false, unique = true)
    private Ordine ordine;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totale;

    @Column(nullable = false)
    private Boolean pagato;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoPagamento metodo;

    @Column
    private LocalDateTime pagamentoIl;

    public Conto() {
    }

    public Conto(Ordine ordine, BigDecimal totale, Boolean pagato, MetodoPagamento metodo) {
        this.ordine = ordine;
        this.totale = totale;
        this.pagato = pagato;
        this.metodo = metodo;
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

    public BigDecimal getTotale() {
        return totale;
    }

    public void setTotale(BigDecimal totale) {
        this.totale = totale;
    }

    public Boolean getPagato() {
        return pagato;
    }

    public void setPagato(Boolean pagato) {
        this.pagato = pagato;
    }

    public MetodoPagamento getMetodo() {
        return metodo;
    }

    public void setMetodo(MetodoPagamento metodo) {
        this.metodo = metodo;
    }

    public LocalDateTime getPagamentoIl() {
        return pagamentoIl;
    }

    public void setPagamentoIl(LocalDateTime pagamentoIl) {
        this.pagamentoIl = pagamentoIl;
    }
}
