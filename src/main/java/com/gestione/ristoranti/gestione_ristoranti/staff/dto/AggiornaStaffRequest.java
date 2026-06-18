package com.gestione.ristoranti.gestione_ristoranti.staff.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class AggiornaStaffRequest {
    private String nome;

    @Email(message = "Formato email non valido")
    private String email;

    private String ruolo;
}
