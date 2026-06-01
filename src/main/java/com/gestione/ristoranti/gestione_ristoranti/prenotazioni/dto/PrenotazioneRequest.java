package com.gestione.ristoranti.gestione_ristoranti.prenotazioni.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public class PrenotazioneRequest {

    @NotNull
    private Long tavoloId;

    @NotNull
    @FutureOrPresent
    private LocalDate data;

    @NotNull
    private LocalTime ora;

    @NotNull
    @Min(1)
    private Integer coperti;

    private String note;

    public PrenotazioneRequest() {}

    public Long getTavoloId() { return tavoloId; }
    public void setTavoloId(Long tavoloId) { this.tavoloId = tavoloId; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getOra() { return ora; }
    public void setOra(LocalTime ora) { this.ora = ora; }

    public Integer getCoperti() { return coperti; }
    public void setCoperti(Integer coperti) { this.coperti = coperti; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
