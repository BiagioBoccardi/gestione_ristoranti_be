package com.gestione.ristoranti.gestione_ristoranti.ordini.repository;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.StatoTavolo;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Tavolo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TavoloRepository extends JpaRepository<Tavolo, Long> {

    List<Tavolo> findByStato(StatoTavolo stato);

    Optional<Tavolo> findByNumero(Integer numero);

    boolean existsByNumero(Integer numero);

    Optional<Tavolo> findByQrToken(String qrToken);
}
