package com.gestione.ristoranti.gestione_ristoranti.analytics;

import com.gestione.ristoranti.gestione_ristoranti.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AnalyticsControllerTest extends AbstractIntegrationTest {

    private static final String DA = LocalDate.now().minusDays(30).toString();
    private static final String A  = LocalDate.now().toString();

    // ── GET /kpi ──────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getKpi_admin_200() throws Exception {
        mockMvc.perform(get("/api/analytics/kpi")
                        .param("da", DA)
                        .param("a", A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser(roles = "CAMERIERE")
    void getKpi_cameriere_403() throws Exception {
        mockMvc.perform(get("/api/analytics/kpi")
                        .param("da", DA)
                        .param("a", A))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void getKpi_nonAutenticato_401() throws Exception {
        mockMvc.perform(get("/api/analytics/kpi")
                        .param("da", DA)
                        .param("a", A))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /revenue/giornaliero ──────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void revenueGiornaliera_admin_200() throws Exception {
        mockMvc.perform(get("/api/analytics/revenue/giornaliero")
                        .param("giorni", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "CUOCO")
    void revenueGiornaliera_cuoco_403() throws Exception {
        mockMvc.perform(get("/api/analytics/revenue/giornaliero"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    // ── GET /revenue/settimanale ──────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void revenueSettimanale_admin_200() throws Exception {
        mockMvc.perform(get("/api/analytics/revenue/settimanale")
                        .param("settimane", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ── GET /top-piatti ───────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void topPiatti_admin_200() throws Exception {
        mockMvc.perform(get("/api/analytics/top-piatti")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ── GET /metodi-pagamento ─────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void metodiPagamento_admin_200() throws Exception {
        mockMvc.perform(get("/api/analytics/metodi-pagamento")
                        .param("da", DA)
                        .param("a", A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ── GET /food-cost ────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void foodCostPerPiatto_admin_200() throws Exception {
        mockMvc.perform(get("/api/analytics/food-cost"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void foodCostPeriodo_admin_200() throws Exception {
        mockMvc.perform(get("/api/analytics/food-cost/periodo")
                        .param("da", DA)
                        .param("a", A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }
}
