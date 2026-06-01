package com.gestione.ristoranti.gestione_ristoranti.menu.repository;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.Categoria;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Piatto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PiattoRepository extends JpaRepository<Piatto, Long> {
    List<Piatto> findByCategoria(Categoria categoria);
    List<Piatto> findByDisponibile(boolean disponibile);
    List<Piatto> findByCategoriaAndDisponibile(Categoria categoria, boolean disponibile);

    boolean existsByCategoriaId(Long categoriaId);
}
