package com.gestione.ristoranti.gestione_ristoranti.conti.dto;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.MetodoPagamento;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PagaContoRequest {

    @NotNull(message = "Il metodo di pagamento è obbligatorio")
    private MetodoPagamento metodo;
}
