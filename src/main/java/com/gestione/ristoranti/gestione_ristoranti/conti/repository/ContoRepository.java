package com.gestione.ristoranti.gestione_ristoranti.conti.repository;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.Conto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContoRepository extends JpaRepository<Conto, Long> {

    Optional<Conto> findByOrdineId(Long ordineId);

    boolean existsByOrdineId(Long ordineId);
}
