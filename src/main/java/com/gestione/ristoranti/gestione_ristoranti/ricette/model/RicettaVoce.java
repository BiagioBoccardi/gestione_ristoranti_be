package com.gestione.ristoranti.gestione_ristoranti.ricette.model;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.Piatto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "ricetta_voci")
public class RicettaVoce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "piatto_id", nullable = false)
    private Piatto piatto;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ingrediente_id", nullable = false)
    private Ingrediente ingrediente;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal quantita;

    // percentuale di scarto/calo peso (es. 10.00 = 10%)
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualeScarto;

    public RicettaVoce() {}

    public RicettaVoce(Piatto piatto, Ingrediente ingrediente, BigDecimal quantita, BigDecimal percentualeScarto) {
        this.piatto = piatto;
        this.ingrediente = ingrediente;
        this.quantita = quantita;
        this.percentualeScarto = percentualeScarto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Piatto getPiatto() { return piatto; }
    public void setPiatto(Piatto piatto) { this.piatto = piatto; }

    public Ingrediente getIngrediente() { return ingrediente; }
    public void setIngrediente(Ingrediente ingrediente) { this.ingrediente = ingrediente; }

    public BigDecimal getQuantita() { return quantita; }
    public void setQuantita(BigDecimal quantita) { this.quantita = quantita; }

    public BigDecimal getPercentualeScarto() { return percentualeScarto; }
    public void setPercentualeScarto(BigDecimal percentualeScarto) { this.percentualeScarto = percentualeScarto; }
}
