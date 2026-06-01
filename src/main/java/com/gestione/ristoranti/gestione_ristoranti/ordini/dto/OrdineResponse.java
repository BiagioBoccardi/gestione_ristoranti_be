package com.gestione.ristoranti.gestione_ristoranti.ordini.dto;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.StatoOrdine;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrdineResponse {

    private Long id;
    private Long tavoloId;
    private Integer numeroTavolo;
    private Long utenteId;
    private String nomeUtente;
    private StatoOrdine stato;
    private List<OrdineItemResponse> items;
    private BigDecimal totale;
    private LocalDateTime creatoAt;
}
