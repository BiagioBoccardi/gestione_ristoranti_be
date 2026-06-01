package com.gestione.ristoranti.gestione_ristoranti.ordini.repository;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.Ordine;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.StatoOrdine;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Tavolo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrdineRepository extends JpaRepository<Ordine, Long> {

    List<Ordine> findByTavolo(Tavolo tavolo);

    List<Ordine> findByStato(StatoOrdine stato);

    List<Ordine> findByUtenteId(Long utenteId);

    List<Ordine> findByTavoloAndStato(Tavolo tavolo, StatoOrdine stato);

    @Query("SELECT DISTINCT o FROM Ordine o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.piatto")
    List<Ordine> findAllWithItems();

    @Query("SELECT DISTINCT o FROM Ordine o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.piatto WHERE o.tavolo = :tavolo")
    List<Ordine> findByTavoloWithItems(@Param("tavolo") Tavolo tavolo);

    @Query("SELECT DISTINCT o FROM Ordine o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.piatto WHERE o.stato = :stato")
    List<Ordine> findByStatoWithItems(@Param("stato") StatoOrdine stato);

    @Query("SELECT DISTINCT o FROM Ordine o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.piatto WHERE o.id = :id")
    Optional<Ordine> findByIdWithItems(@Param("id") Long id);
}
