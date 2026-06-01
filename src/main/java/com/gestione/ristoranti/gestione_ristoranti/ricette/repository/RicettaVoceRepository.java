package com.gestione.ristoranti.gestione_ristoranti.ricette.repository;

import com.gestione.ristoranti.gestione_ristoranti.ricette.model.RicettaVoce;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RicettaVoceRepository extends JpaRepository<RicettaVoce, Long> {

    List<RicettaVoce> findByPiattoId(Long piattoId);

    void deleteByPiattoId(Long piattoId);
}
