package com.gestione.ristoranti.gestione_ristoranti.prenotazioni.repository;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.Prenotazione;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Tavolo;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Utente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface PrenotazioneRepository extends JpaRepository<Prenotazione, Long> {

    List<Prenotazione> findByUtente(Utente utente);

    List<Prenotazione> findByData(LocalDate data);

    List<Prenotazione> findByTavoloAndData(Tavolo tavolo, LocalDate data);

    boolean existsByTavoloAndDataAndOraBetween(Tavolo tavolo, LocalDate data, LocalTime oraInizio, LocalTime oraFine);
}
