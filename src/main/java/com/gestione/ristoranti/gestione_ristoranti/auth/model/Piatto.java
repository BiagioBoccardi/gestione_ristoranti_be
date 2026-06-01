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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "piatti", indexes = {
    @Index(name = "idx_piatti_categoria", columnList = "categoria_id"),
    @Index(name = "idx_piatti_disponibile", columnList = "disponibile")
})
public class Piatto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 500)
    private String descrizione;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prezzo;

    @Column(nullable = false)
    private Boolean disponibile;

    @Column(length = 255)
    private String foto;

    @OneToMany(mappedBy = "piatto", fetch = FetchType.LAZY)
    private List<OrdineItem> ordineItems = new ArrayList<>();

    public Piatto() {
    }

    public Piatto(Categoria categoria, String nome, BigDecimal prezzo, Boolean disponibile, String foto) {
        this.categoria = categoria;
        this.nome = nome;
        this.prezzo = prezzo;
        this.disponibile = disponibile;
        this.foto = foto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public BigDecimal getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(BigDecimal prezzo) {
        this.prezzo = prezzo;
    }

    public Boolean getDisponibile() {
        return disponibile;
    }

    public void setDisponibile(Boolean disponibile) {
        this.disponibile = disponibile;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public List<OrdineItem> getOrdineItems() {
        return ordineItems;
    }

    public void setOrdineItems(List<OrdineItem> ordineItems) {
        this.ordineItems = ordineItems;
    }
}
