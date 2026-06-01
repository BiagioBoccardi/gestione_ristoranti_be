package com.gestione.ristoranti.gestione_ristoranti.staff.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StaffDetailResponse {
    private Long id;
    private String nome;
    private String email;
    private String ruolo;
    private List<TurnoResponse> turni;
}
