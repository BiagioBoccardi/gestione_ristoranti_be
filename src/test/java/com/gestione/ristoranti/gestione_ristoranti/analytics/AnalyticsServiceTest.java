package com.gestione.ristoranti.gestione_ristoranti.analytics;

import com.gestione.ristoranti.gestione_ristoranti.analytics.dto.KpiResponse;
import com.gestione.ristoranti.gestione_ristoranti.analytics.repository.AnalyticsRepository;
import com.gestione.ristoranti.gestione_ristoranti.analytics.service.AnalyticsService;
import com.gestione.ristoranti.gestione_ristoranti.ricette.service.RicetteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock private AnalyticsRepository analyticsRepository;
    @Mock private RicetteService ricetteService;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void getKpi_conOrdini_calcolaValoreMedioCorrettamente() {
        LocalDate da = LocalDate.now().minusDays(7);
        LocalDate a = LocalDate.now();

        when(analyticsRepository.revenuePeriodo(any(), any())).thenReturn(new BigDecimal("200.00"));
        when(analyticsRepository.ordiniCompletatiPeriodo(any(), any())).thenReturn(4L);
        when(analyticsRepository.copertiMedi(any(), any())).thenReturn(2.5);
        // foodCostPerPiatto → lista vuota, food cost medio = 0
        when(analyticsRepository.piattiPerFoodCost()).thenReturn(List.of());

        KpiResponse kpi = analyticsService.getKpi(da, a);

        assertThat(kpi.getRevenueTotale()).isEqualByComparingTo("200.00");
        assertThat(kpi.getOrdiniCompletati()).isEqualTo(4L);
        assertThat(kpi.getValoremedioOrdine()).isEqualByComparingTo("50.00");
        assertThat(kpi.getCopertiMediPrenotazione()).isEqualTo(2.5);
    }

    @Test
    void getKpi_senzaOrdini_valorMedioZero() {
        LocalDate da = LocalDate.now().minusDays(7);
        LocalDate a = LocalDate.now();

        when(analyticsRepository.revenuePeriodo(any(), any())).thenReturn(BigDecimal.ZERO);
        when(analyticsRepository.ordiniCompletatiPeriodo(any(), any())).thenReturn(0L);
        when(analyticsRepository.copertiMedi(any(), any())).thenReturn(null);
        when(analyticsRepository.piattiPerFoodCost()).thenReturn(List.of());

        KpiResponse kpi = analyticsService.getKpi(da, a);

        assertThat(kpi.getValoremedioOrdine()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(kpi.getCopertiMediPrenotazione()).isEqualTo(0.0);
    }

    @Test
    void revenueGiornaliera_mappaRigheCorrettamente() {
        // Riga: [data_str, revenue, ordini]
        Object[] row = new Object[]{"2024-01-15", new BigDecimal("150.00"), 3L};
        when(analyticsRepository.revenueGiornaliera(any(), any())).thenReturn(java.util.Collections.singletonList(row));

        var result = analyticsService.revenueGiornaliera(7);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEtichetta()).isEqualTo("01/15");
        assertThat(result.get(0).getRevenue()).isEqualByComparingTo("150.00");
        assertThat(result.get(0).getOrdini()).isEqualTo(3L);
    }

    @Test
    void topPiatti_limitaRisultati() {
        Object[] p1 = {1L, "Pizza", 50L, new BigDecimal("500.00")};
        Object[] p2 = {2L, "Pasta", 40L, new BigDecimal("360.00")};
        Object[] p3 = {3L, "Risotto", 30L, new BigDecimal("270.00")};
        when(analyticsRepository.topPiatti()).thenReturn(List.of(p1, p2, p3));

        var result = analyticsService.topPiatti(2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNome()).isEqualTo("Pizza");
    }
}
