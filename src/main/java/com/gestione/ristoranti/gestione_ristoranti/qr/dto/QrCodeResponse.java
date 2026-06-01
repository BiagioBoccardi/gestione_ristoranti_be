package com.gestione.ristoranti.gestione_ristoranti.qr.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QrCodeResponse {
    private Long tavoloId;
    private Integer numeroTavolo;
    private String qrToken;
    private String menuUrl;
}