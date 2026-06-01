package com.gestione.ristoranti.gestione_ristoranti.conti.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ContoItemResponse {

    private Long id;
    private String nomePiatto;
    private Integer quantita;
    private BigDecimal prezzoUnitario;
    private BigDecimal subtotale;
    private String note;
}
