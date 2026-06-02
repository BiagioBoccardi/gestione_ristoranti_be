package com.gestione.ristoranti.gestione_ristoranti.analytics.repository;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.Conto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AnalyticsRepository extends JpaRepository<Conto, Long> {

    // Revenue totale: usa pagamentoIl se presente, altrimenti creatoIl dell'ordine
    @Query("SELECT COALESCE(SUM(c.totale), 0) FROM Conto c " +
           "WHERE c.pagato = true " +
           "AND COALESCE(c.pagamentoIl, c.ordine.creatoIl) BETWEEN :da AND :a")
    BigDecimal revenuePeriodo(@Param("da") LocalDateTime da, @Param("a") LocalDateTime a);

    // Ordini CONSEGNATO nel periodo (non richiede conto pagato)
    @Query("SELECT COUNT(o) FROM Ordine o " +
           "WHERE o.stato = com.gestione.ristoranti.gestione_ristoranti.auth.model.StatoOrdine.CONSEGNATO " +
           "AND o.creatoIl BETWEEN :da AND :a")
    Long ordiniCompletatiPeriodo(@Param("da") LocalDateTime da, @Param("a") LocalDateTime a);

    // Revenue giornaliera con fallback su creatoIl
    @Query(value = "SELECT CAST(COALESCE(c.pagamento_il, o.creato_il) AS DATE) AS giorno, " +
                   "COALESCE(SUM(c.totale), 0), COUNT(c.id) " +
                   "FROM conti c JOIN ordini o ON c.ordine_id = o.id " +
                   "WHERE c.pagato = true " +
                   "AND COALESCE(c.pagamento_il, o.creato_il) BETWEEN :da AND :a " +
                   "GROUP BY giorno ORDER BY giorno",
           nativeQuery = true)
    List<Object[]> revenueGiornaliera(@Param("da") LocalDateTime da, @Param("a") LocalDateTime a);

    // Revenue settimanale con fallback su creatoIl
    @Query(value = "SELECT TO_CHAR(COALESCE(c.pagamento_il, o.creato_il), 'IYYY-IW') AS settimana, " +
                   "COALESCE(SUM(c.totale), 0), COUNT(c.id) " +
                   "FROM conti c JOIN ordini o ON c.ordine_id = o.id " +
                   "WHERE c.pagato = true " +
                   "AND COALESCE(c.pagamento_il, o.creato_il) BETWEEN :da AND :a " +
                   "GROUP BY settimana ORDER BY settimana",
           nativeQuery = true)
    List<Object[]> revenueSettimanale(@Param("da") LocalDateTime da, @Param("a") LocalDateTime a);

    // Top piatti da ordini CONSEGNATO (non richiede conto)
    @Query("SELECT oi.piatto.id, oi.piatto.nome, SUM(oi.quantita), " +
           "SUM(oi.quantita * COALESCE(oi.prezzoUnitario, oi.piatto.prezzo)) " +
           "FROM OrdineItem oi " +
           "WHERE oi.ordine.stato = com.gestione.ristoranti.gestione_ristoranti.auth.model.StatoOrdine.CONSEGNATO " +
           "GROUP BY oi.piatto.id, oi.piatto.nome " +
           "ORDER BY SUM(oi.quantita) DESC")
    List<Object[]> topPiatti();

    // Metodi pagamento con fallback su creatoIl
    @Query("SELECT c.metodo, COUNT(c), SUM(c.totale) FROM Conto c " +
           "WHERE c.pagato = true " +
           "AND COALESCE(c.pagamentoIl, c.ordine.creatoIl) BETWEEN :da AND :a " +
           "GROUP BY c.metodo")
    List<Object[]> metodiPagamento(@Param("da") LocalDateTime da, @Param("a") LocalDateTime a);

    // Coperti medi dalle prenotazioni
    @Query("SELECT AVG(p.coperti) FROM Prenotazione p WHERE p.data BETWEEN :da AND :a")
    Double copertiMedi(@Param("da") LocalDate da, @Param("a") LocalDate a);

    // Lista piatti con ricetta per food cost analytics
    @Query("SELECT rv.piatto.id, rv.piatto.nome, rv.piatto.prezzo FROM RicettaVoce rv " +
           "GROUP BY rv.piatto.id, rv.piatto.nome, rv.piatto.prezzo")
    List<Object[]> piattiPerFoodCost();
}
