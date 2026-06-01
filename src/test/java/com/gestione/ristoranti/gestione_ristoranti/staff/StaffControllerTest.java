package com.gestione.ristoranti.gestione_ristoranti.staff;

import com.gestione.ristoranti.gestione_ristoranti.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StaffControllerTest extends AbstractIntegrationTest {

    // ── GET staff ─────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getStaff_admin_200() throws Exception {
        mockMvc.perform(get("/api/staff"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "CAMERIERE")
    void getStaff_cameriere_403() throws Exception {
        mockMvc.perform(get("/api/staff"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void getStaff_nonAutenticato_401() throws Exception {
        mockMvc.perform(get("/api/staff"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getStaffById_nonEsistente_404() throws Exception {
        mockMvc.perform(get("/api/staff/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ── GET turni ─────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTurni_admin_200() throws Exception {
        mockMvc.perform(get("/api/staff/turni"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "CUOCO")
    void getTurni_cuoco_403() throws Exception {
        mockMvc.perform(get("/api/staff/turni"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTurniByUtente_nonEsistente_404() throws Exception {
        mockMvc.perform(get("/api/staff/999999/turni"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ── POST turno ────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void creaTurno_campiMancanti_400() throws Exception {
        mockMvc.perform(post("/api/staff/turni")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser(roles = "CAMERIERE")
    void creaTurno_cameriere_403() throws Exception {
        mockMvc.perform(post("/api/staff/turni")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"utenteId\":1,\"dataInizio\":\"2099-01-01T09:00:00\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    // ── PUT turno ─────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void aggiornaTurno_nonEsistente_404() throws Exception {
        mockMvc.perform(put("/api/staff/turni/999999")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"utenteId\":1,\"dataInizio\":\"2099-01-01T09:00:00\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ── DELETE turno ──────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminaTurno_nonEsistente_404() throws Exception {
        mockMvc.perform(delete("/api/staff/turni/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = "CAMERIERE")
    void eliminaTurno_cameriere_403() throws Exception {
        mockMvc.perform(delete("/api/staff/turni/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
