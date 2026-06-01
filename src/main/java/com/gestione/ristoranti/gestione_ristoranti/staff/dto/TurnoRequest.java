package com.gestione.ristoranti.gestione_ristoranti.staff.dto;

import com.gestione.ristoranti.gestione_ristoranti.staff.model.StatoTurno;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TurnoRequest {

    @NotNull(message = "L'id utente è obbligatorio")
    private Long utenteId;

    @NotNull(message = "La data di inizio è obbligatoria")
    private LocalDateTime dataInizio;

    private LocalDateTime dataFine;

    private StatoTurno stato;

    private String note;
}
