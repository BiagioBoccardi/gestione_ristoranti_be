package com.gestione.ristoranti.gestione_ristoranti.ordini.dto;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.StatoTavolo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TavoloRequest {

    @NotNull(message = "Il numero tavolo è obbligatorio")
    private Integer numero;

    @NotNull(message = "Il numero di coperti è obbligatorio")
    @Min(value = 1, message = "I coperti devono essere almeno 1")
    private Integer coperti;

    @NotNull(message = "Lo stato è obbligatorio")
    private StatoTavolo stato;
}
