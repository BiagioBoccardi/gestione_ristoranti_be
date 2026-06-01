package com.gestione.ristoranti.gestione_ristoranti.menu.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PiattoRequest {
    @NotNull(message = "L'ID categoria è obbligatorio")
    private Long categoriaId;

    @NotBlank(message = "Il nome è obbligatorio")
    @Size(max = 150, message = "Il nome non può superare i 150 caratteri")
    private String nome;

    @Size(max = 500, message = "La descrizione non può superare i 500 caratteri")
    private String descrizione;

    @NotNull(message = "Il prezzo è obbligatorio")
    @DecimalMin(value = "0.01", message = "Il prezzo deve essere maggiore di zero")
    @Digits(integer = 8, fraction = 2, message = "Il prezzo può avere al massimo 2 decimali")
    private BigDecimal prezzo;

    @NotNull(message = "La disponibilità è obbligatoria")
    private Boolean disponibile;

    @Size(max = 255, message = "L'URL dell'immagine non può superare i 255 caratteri")
    private String immagineUrl;

}
