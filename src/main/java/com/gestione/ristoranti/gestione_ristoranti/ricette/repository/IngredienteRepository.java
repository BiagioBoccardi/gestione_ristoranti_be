package com.gestione.ristoranti.gestione_ristoranti.ricette.repository;

import com.gestione.ristoranti.gestione_ristoranti.ricette.model.Ingrediente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {

    List<Ingrediente> findAllByOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);
}
