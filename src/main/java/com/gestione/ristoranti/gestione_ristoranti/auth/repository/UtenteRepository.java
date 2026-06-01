package com.gestione.ristoranti.gestione_ristoranti.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.Utente;

import java.util.List;
import java.util.Optional;

public interface UtenteRepository extends JpaRepository<Utente, Long> {
    Optional<Utente> findByEmail(String email);
    List<Utente> findByRuoloNomeNot(String ruoloNome);
    boolean existsByEmail(String email);
}
