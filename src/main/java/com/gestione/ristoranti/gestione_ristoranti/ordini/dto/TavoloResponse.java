package com.gestione.ristoranti.gestione_ristoranti.ordini.dto;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.StatoTavolo;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TavoloResponse {

    private Long id;
    private Integer numero;
    private Integer coperti;
    private StatoTavolo stato;
    private String qrToken;
}
