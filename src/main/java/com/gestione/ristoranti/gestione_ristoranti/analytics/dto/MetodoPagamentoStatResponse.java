package com.gestione.ristoranti.gestione_ristoranti.analytics.dto;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.MetodoPagamento;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class MetodoPagamentoStatResponse {

    private MetodoPagamento metodo;
    private Long conteggio;
    private BigDecimal totale;
}
