package com.gestione.ristoranti.gestione_ristoranti.staff.dto;

import com.gestione.ristoranti.gestione_ristoranti.staff.model.StatoTurno;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TurnoResponse {
    private Long id;
    private Long utenteId;
    private String utenteNome;
    private LocalDateTime dataInizio;
    private LocalDateTime dataFine;
    private StatoTurno stato;
    private String note;
}
