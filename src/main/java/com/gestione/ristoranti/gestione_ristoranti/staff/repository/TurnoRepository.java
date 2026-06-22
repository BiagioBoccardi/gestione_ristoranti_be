package com.gestione.ristoranti.gestione_ristoranti.staff.repository;

import com.gestione.ristoranti.gestione_ristoranti.staff.model.StatoTurno;
import com.gestione.ristoranti.gestione_ristoranti.staff.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TurnoRepository extends JpaRepository<Turno, Long> {
    List<Turno> findByUtenteId(Long utenteId);
    List<Turno> findByStato(StatoTurno stato);
    List<Turno> findByDataInizioBetween(LocalDateTime da, LocalDateTime a);
    long countByUtenteId(Long utenteId);
    void deleteByUtenteId(Long utenteId);
}
