package com.gestione.ristoranti.gestione_ristoranti.ordini.dto;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.StatoOrdine;

import java.time.LocalDateTime;

public record OrdineStatoEvent(
        Long ordineId,
        Long tavoloId,
        int numeroTavolo,
        StatoOrdine statoVecchio,
        StatoOrdine statoNuovo,
        LocalDateTime timestamp
) {}