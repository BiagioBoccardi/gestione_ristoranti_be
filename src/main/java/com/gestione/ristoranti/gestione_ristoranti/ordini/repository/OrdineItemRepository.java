package com.gestione.ristoranti.gestione_ristoranti.ordini.repository;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.OrdineItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdineItemRepository extends JpaRepository<OrdineItem, Long> {

    List<OrdineItem> findByOrdineId(Long ordineId);

    void deleteByPiattoId(Long piattoId);
}
