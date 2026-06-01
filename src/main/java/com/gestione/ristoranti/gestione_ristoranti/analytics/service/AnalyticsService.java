package com.gestione.ristoranti.gestione_ristoranti.analytics.service;

import com.gestione.ristoranti.gestione_ristoranti.analytics.dto.FoodCostResponse;
import com.gestione.ristoranti.gestione_ristoranti.analytics.dto.KpiResponse;
import com.gestione.ristoranti.gestione_ristoranti.analytics.dto.MetodoPagamentoStatResponse;
import com.gestione.ristoranti.gestione_ristoranti.analytics.dto.RevenuePointResponse;
import com.gestione.ristoranti.gestione_ristoranti.analytics.dto.TopPiattoResponse;
import com.gestione.ristoranti.gestione_ristoranti.analytics.repository.AnalyticsRepository;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.MetodoPagamento;
import com.gestione.ristoranti.gestione_ristoranti.ricette.service.RicetteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final RicetteService ricetteService;

    // ── KPI ──────────────────────────────────────────────────────────────────

    /**
     * Calcola i KPI principali per il periodo indicato: revenue totale, ordini completati,
     * valore medio ordine, coperti medi da prenotazione e food cost medio percentuale.
     *
     * @param da data di inizio periodo (inclusa)
     * @param a  data di fine periodo (inclusa)
     * @return oggetto KPI con tutte le metriche aggregate
     */
    public KpiResponse getKpi(LocalDate da, LocalDate a) {
        LocalDateTime daTime = da.atStartOfDay();
        LocalDateTime aTime = a.atTime(23, 59, 59);

        BigDecimal revenue = analyticsRepository.revenuePeriodo(daTime, aTime);
        Long ordini = analyticsRepository.ordiniCompletatiPeriodo(daTime, aTime);
        Double copertiMedi = analyticsRepository.copertiMedi(da, a);

        BigDecimal valMedio = ordini > 0
                ? revenue.divide(BigDecimal.valueOf(ordini), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal foodCostMedio = calcolaFoodCostMedio();

        return KpiResponse.builder()
                .revenueTotale(revenue)
                .ordiniCompletati(ordini)
                .valoremedioOrdine(valMedio)
                .copertiMediPrenotazione(copertiMedi != null ? Math.round(copertiMedi * 10.0) / 10.0 : 0.0)
                .foodCostMedioPercentuale(foodCostMedio)
                .build();
    }

    // ── Revenue giornaliera ───────────────────────────────────────────────────

    public List<RevenuePointResponse> revenueGiornaliera(int giorni) {
        LocalDateTime a = LocalDateTime.now();
        LocalDateTime da = a.minusDays(giorni - 1L).toLocalDate().atStartOfDay();

        List<Object[]> rows = analyticsRepository.revenueGiornaliera(da, a);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");

        return rows.stream().map(r -> RevenuePointResponse.builder()
                .etichetta(r[0].toString().substring(5).replace("-", "/")) // MM-DD → MM/DD
                .revenue(toBigDecimal(r[1]))
                .ordini(toLong(r[2]))
                .build())
                .collect(Collectors.toList());
    }

    // ── Revenue settimanale ───────────────────────────────────────────────────

    public List<RevenuePointResponse> revenueSettimanale(int settimane) {
        LocalDateTime a = LocalDateTime.now();
        LocalDateTime da = a.minusWeeks(settimane - 1L).toLocalDate().atStartOfDay();

        return analyticsRepository.revenueSettimanale(da, a).stream()
                .map(r -> RevenuePointResponse.builder()
                        .etichetta("Sett. " + r[0].toString().split("-")[1])
                        .revenue(toBigDecimal(r[1]))
                        .ordini(toLong(r[2]))
                        .build())
                .collect(Collectors.toList());
    }

    // ── Top piatti ────────────────────────────────────────────────────────────

    public List<TopPiattoResponse> topPiatti(int limit) {
        return analyticsRepository.topPiatti().stream()
                .limit(limit)
                .map(r -> TopPiattoResponse.builder()
                        .piattoId(toLong(r[0]))
                        .nome(r[1].toString())
                        .quantitaVenduta(toLong(r[2]))
                        .revenueGenerata(toBigDecimal(r[3]))
                        .build())
                .collect(Collectors.toList());
    }

    // ── Metodi pagamento ──────────────────────────────────────────────────────

    public List<MetodoPagamentoStatResponse> metodiPagamento(LocalDate da, LocalDate a) {
        LocalDateTime daTime = da.atStartOfDay();
        LocalDateTime aTime = a.atTime(23, 59, 59);

        return analyticsRepository.metodiPagamento(daTime, aTime).stream()
                .map(r -> MetodoPagamentoStatResponse.builder()
                        .metodo(MetodoPagamento.valueOf(r[0].toString()))
                        .conteggio(toLong(r[1]))
                        .totale(toBigDecimal(r[2]))
                        .build())
                .collect(Collectors.toList());
    }

    // ── Food cost per piatto ──────────────────────────────────────────────────

    /**
     * Calcola il food cost percentuale per ogni piatto del menu.
     * Il giudizio è: OTTIMO ≤25%, BUONO ≤32%, ATTENZIONE ≤40%, CRITICO &gt;40%.
     * I risultati sono ordinati per food cost decrescente.
     *
     * @return lista di piatti con costo porzione, prezzo vendita e percentuale food cost
     */
    public List<FoodCostResponse> foodCostPerPiatto() {
        List<Object[]> piatti = analyticsRepository.piattiPerFoodCost();
        List<FoodCostResponse> result = new ArrayList<>();

        for (Object[] row : piatti) {
            Long piattoId = toLong(row[0]);
            String nome = row[1].toString();
            BigDecimal prezzo = toBigDecimal(row[2]);

            BigDecimal costo = ricetteService.calcolaCostoPorzione(piattoId);
            BigDecimal fc = prezzo.compareTo(BigDecimal.ZERO) > 0
                    ? costo.divide(prezzo, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            result.add(FoodCostResponse.builder()
                    .piattoId(piattoId)
                    .nomePiatto(nome)
                    .prezzoVendita(prezzo)
                    .costoPorzione(costo.setScale(2, RoundingMode.HALF_UP))
                    .foodCostPercentuale(fc)
                    .giudizio(giudizio(fc))
                    .build());
        }

        result.sort((a2, b) -> b.getFoodCostPercentuale().compareTo(a2.getFoodCostPercentuale()));
        return result;
    }

    // ── Food cost medio ponderato del periodo ─────────────────────────────────

    public BigDecimal foodCostMedioPeriodo(LocalDate da, LocalDate a) {
        LocalDateTime daTime = da.atStartOfDay();
        LocalDateTime aTime = a.atTime(23, 59, 59);

        BigDecimal revenue = analyticsRepository.revenuePeriodo(daTime, aTime);
        if (revenue.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        // Somma costi reali dagli ordini del periodo
        List<Object[]> top = analyticsRepository.topPiatti();
        BigDecimal costoTotale = BigDecimal.ZERO;
        for (Object[] row : top) {
            Long piattoId = toLong(row[0]);
            long qta = toLong(row[2]);
            BigDecimal costoPorzione = ricetteService.calcolaCostoPorzione(piattoId);
            costoTotale = costoTotale.add(costoPorzione.multiply(BigDecimal.valueOf(qta)));
        }

        return costoTotale.divide(revenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BigDecimal calcolaFoodCostMedio() {
        List<FoodCostResponse> lista = foodCostPerPiatto();
        if (lista.isEmpty()) return BigDecimal.ZERO;
        BigDecimal somma = lista.stream()
                .map(FoodCostResponse::getFoodCostPercentuale)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return somma.divide(BigDecimal.valueOf(lista.size()), 2, RoundingMode.HALF_UP);
    }

    private String giudizio(BigDecimal fc) {
        double v = fc.doubleValue();
        if (v <= 25) return "OTTIMO";
        if (v <= 32) return "BUONO";
        if (v <= 40) return "ATTENZIONE";
        return "CRITICO";
    }

    private BigDecimal toBigDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        return new BigDecimal(o.toString());
    }

    private Long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Long) return (Long) o;
        if (o instanceof Number) return ((Number) o).longValue();
        return Long.parseLong(o.toString());
    }
}
