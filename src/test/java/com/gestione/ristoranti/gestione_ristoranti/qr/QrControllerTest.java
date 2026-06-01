package com.gestione.ristoranti.gestione_ristoranti.qr;

import com.gestione.ristoranti.gestione_ristoranti.AbstractIntegrationTest;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.TavoloRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class QrControllerTest extends AbstractIntegrationTest {

    @Autowired
    private TavoloRepository tavoloRepository;

    // ── GET /{tavoloId} ───────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getQrCode_tavoloEsistente_200() throws Exception {
        Long tavoloId = tavoloRepository.findAll().get(0).getId();
        mockMvc.perform(get("/api/qr/" + tavoloId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("image/png")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getQrCode_tavoloNonEsistente_404() throws Exception {
        mockMvc.perform(get("/api/qr/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CAMERIERE")
    void getQrCode_cameriere_403() throws Exception {
        mockMvc.perform(get("/api/qr/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void getQrCode_nonAutenticato_401() throws Exception {
        mockMvc.perform(get("/api/qr/1"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /{tavoloId}/info ──────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getQrInfo_tavoloEsistente_200() throws Exception {
        Long tavoloId = tavoloRepository.findAll().get(0).getId();
        mockMvc.perform(get("/api/qr/" + tavoloId + "/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getQrInfo_nonEsistente_404() throws Exception {
        mockMvc.perform(get("/api/qr/999999/info"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = "CUOCO")
    void getQrInfo_cuoco_403() throws Exception {
        mockMvc.perform(get("/api/qr/1/info"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    // ── POST /{tavoloId}/rigenera ─────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void rigeneraQrCode_tavoloEsistente_200() throws Exception {
        Long tavoloId = tavoloRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/qr/" + tavoloId + "/rigenera"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("image/png")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rigeneraQrCode_nonEsistente_404() throws Exception {
        mockMvc.perform(post("/api/qr/999999/rigenera"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CAMERIERE")
    void rigeneraQrCode_cameriere_403() throws Exception {
        mockMvc.perform(post("/api/qr/1/rigenera"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
