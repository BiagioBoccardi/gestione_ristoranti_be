package com.gestione.ristoranti.gestione_ristoranti.prenotazioni.dto;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.Prenotazione;
import java.time.LocalDate;
import java.time.LocalTime;

public class PrenotazioneResponse {

    private Long id;
    private Long tavoloId;
    private Integer numeroTavolo;
    private LocalDate data;
    private LocalTime ora;
    private Integer coperti;
    private String note;
    private String nomeCliente;
    private String emailCliente;

    public static PrenotazioneResponse from(Prenotazione p) {
        PrenotazioneResponse r = new PrenotazioneResponse();
        r.id = p.getId();
        r.tavoloId = p.getTavolo().getId();
        r.numeroTavolo = p.getTavolo().getNumero();
        r.data = p.getData();
        r.ora = p.getOra();
        r.coperti = p.getCoperti();
        r.note = p.getNote();
        r.nomeCliente = p.getUtente().getNome();
        r.emailCliente = p.getUtente().getEmail();
        return r;
    }

    public Long getId() { return id; }
    public Long getTavoloId() { return tavoloId; }
    public Integer getNumeroTavolo() { return numeroTavolo; }
    public LocalDate getData() { return data; }
    public LocalTime getOra() { return ora; }
    public Integer getCoperti() { return coperti; }
    public String getNote() { return note; }
    public String getNomeCliente() { return nomeCliente; }
    public String getEmailCliente() { return emailCliente; }
}
