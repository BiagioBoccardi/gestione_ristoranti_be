package com.gestione.ristoranti.gestione_ristoranti.ordini;

import com.gestione.ristoranti.gestione_ristoranti.AbstractIntegrationTest;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.TavoloRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TavoloControllerTest extends AbstractIntegrationTest {

    @Autowired
    private TavoloRepository tavoloRepository;

    private Long createdTavoloId;

    @AfterEach
    void cleanup() {
        if (createdTavoloId != null) {
            tavoloRepository.deleteById(createdTavoloId);
            createdTavoloId = null;
        }
    }

    // ── GET tavoli ────────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void getTavoli_autenticato_200() throws Exception {
        mockMvc.perform(get("/api/tavoli"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getTavoli_nonAutenticato_401() throws Exception {
        mockMvc.perform(get("/api/tavoli"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getTavolo_nonEsistente_404() throws Exception {
        mockMvc.perform(get("/api/tavoli/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser
    void getTavolo_esistente_200() throws Exception {
        Long id = tavoloRepository.findAll().get(0).getId();
        mockMvc.perform(get("/api/tavoli/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    // ── POST tavolo ───────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTavolo_admin_201() throws Exception {
        String body = """
                {"numero":99,"coperti":4}
                """;

        String response = mockMvc.perform(post("/api/tavoli")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.numero").value(99))
                .andReturn().getResponse().getContentAsString();

        createdTavoloId = ((Number) JsonPath.read(response, "$.data.id")).longValue();
    }

    @Test
    @WithMockUser(roles = "CAMERIERE")
    void createTavolo_cameriere_403() throws Exception {
        mockMvc.perform(post("/api/tavoli")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numero\":99,\"coperti\":4}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTavolo_campiMancanti_400() throws Exception {
        mockMvc.perform(post("/api/tavoli")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ── PATCH stato ───────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "CAMERIERE")
    void aggiornaStato_cameriere_200() throws Exception {
        Long id = tavoloRepository.findAll().get(0).getId();
        mockMvc.perform(patch("/api/tavoli/" + id + "/stato")
                        .param("nuovoStato", "OCCUPATO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @WithMockUser(roles = "CAMERIERE")
    void aggiornaStato_nonEsistente_404() throws Exception {
        mockMvc.perform(patch("/api/tavoli/999999/stato")
                        .param("nuovoStato", "OCCUPATO"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void aggiornaStato_cliente_403() throws Exception {
        mockMvc.perform(patch("/api/tavoli/1/stato")
                        .param("nuovoStato", "OCCUPATO"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    // ── DELETE tavolo ─────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTavolo_nonEsistente_404() throws Exception {
        mockMvc.perform(delete("/api/tavoli/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = "CAMERIERE")
    void deleteTavolo_cameriere_403() throws Exception {
        mockMvc.perform(delete("/api/tavoli/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
