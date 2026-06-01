package com.gestione.ristoranti.gestione_ristoranti.ricette.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "ingredienti")
public class Ingrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 20)
    private String unitaMisura;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal costoPerUnita;

    public Ingrediente() {}

    public Ingrediente(String nome, String unitaMisura, BigDecimal costoPerUnita) {
        this.nome = nome;
        this.unitaMisura = unitaMisura;
        this.costoPerUnita = costoPerUnita;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getUnitaMisura() { return unitaMisura; }
    public void setUnitaMisura(String unitaMisura) { this.unitaMisura = unitaMisura; }

    public BigDecimal getCostoPerUnita() { return costoPerUnita; }
    public void setCostoPerUnita(BigDecimal costoPerUnita) { this.costoPerUnita = costoPerUnita; }
}
