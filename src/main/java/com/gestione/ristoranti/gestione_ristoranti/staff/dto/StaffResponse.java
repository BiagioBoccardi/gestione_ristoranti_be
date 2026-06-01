package com.gestione.ristoranti.gestione_ristoranti.staff.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StaffResponse {
    private Long id;
    private String nome;
    private String email;
    private String ruolo;
    private long nrTurni;
}
