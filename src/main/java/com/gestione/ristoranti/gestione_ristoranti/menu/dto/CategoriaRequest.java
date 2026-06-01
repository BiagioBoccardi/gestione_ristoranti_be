package com.gestione.ristoranti.gestione_ristoranti.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoriaRequest {
    @NotBlank(message = "Il nome è obbligatorio")
    @Size(max = 100, message = "Il nome non può superare i 100 caratteri")
    private String nome;
    
    @Size(max = 500, message = "La descrizione non può superare i 500 caratteri")
    private String descrizione;
}