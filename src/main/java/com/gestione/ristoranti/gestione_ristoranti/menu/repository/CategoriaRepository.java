package com.gestione.ristoranti.gestione_ristoranti.menu.repository;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Optional<Categoria> findByNome(String nome);
}
