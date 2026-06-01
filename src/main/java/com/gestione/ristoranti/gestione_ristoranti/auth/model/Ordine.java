package com.gestione.ristoranti.gestione_ristoranti.auth.model;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordini", indexes = {
    @Index(name = "idx_ordini_stato", columnList = "stato"),
    @Index(name = "idx_ordini_tavolo", columnList = "tavolo_id"),
    @Index(name = "idx_ordini_utente", columnList = "utente_id")
})
public class Ordine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "tavolo_id", nullable = false)
    private Tavolo tavolo;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "utente_id", nullable = true)
    private Utente utente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoOrdine stato;

    @Column(name = "creato_il", nullable = false)
    private LocalDateTime creatoIl;

    @OneToMany(mappedBy = "ordine", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdineItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "ordine", fetch = FetchType.LAZY)
    private Conto conto;

    public Ordine() {
    }

    public Ordine(Tavolo tavolo, Utente utente, StatoOrdine stato, LocalDateTime creatoIl) {
        this.tavolo = tavolo;
        this.utente = utente;
        this.stato = stato;
        this.creatoIl = creatoIl;
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

    public StatoOrdine getStato() {
        return stato;
    }

    public void setStato(StatoOrdine stato) {
        this.stato = stato;
    }

    public LocalDateTime getCreatoIl() {
        return creatoIl;
    }

    public void setCreatoIl(LocalDateTime creatoIl) {
        this.creatoIl = creatoIl;
    }

    public List<OrdineItem> getItems() {
        return items;
    }

    public void setItems(List<OrdineItem> items) {
        this.items = items;
    }

    public Conto getConto() {
        return conto;
    }

    public void setConto(Conto conto) {
        this.conto = conto;
    }
}
