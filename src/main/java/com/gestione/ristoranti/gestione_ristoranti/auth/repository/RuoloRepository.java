package com.gestione.ristoranti.gestione_ristoranti.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.Ruolo;

import java.util.Optional;

public interface RuoloRepository extends JpaRepository<Ruolo, Long> {
    Optional<Ruolo> findByNome(String nome);
}
