package com.gestione.ristoranti.gestione_ristoranti.conti.dto;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.MetodoPagamento;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ContoResponse {

    private Long id;
    private Long ordineId;
    private Integer numeroTavolo;
    private List<ContoItemResponse> items;
    private BigDecimal totale;
    private Boolean pagato;
    private MetodoPagamento metodo;
    private LocalDateTime pagamentoIl;
}
