package com.gestione.ristoranti.gestione_ristoranti.ordini;

import com.gestione.ristoranti.gestione_ristoranti.AbstractIntegrationTest;
import com.gestione.ristoranti.gestione_ristoranti.menu.repository.PiattoRepository;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.OrdineRepository;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.TavoloRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrdineControllerTest extends AbstractIntegrationTest {

    @Autowired
    private TavoloRepository tavoloRepository;

    @Autowired
    private PiattoRepository piattoRepository;

    @Autowired
    private OrdineRepository ordineRepository;

    private Long createdOrdineId;

    @AfterEach
    void cleanup() {
        if (createdOrdineId != null) {
            ordineRepository.deleteById(createdOrdineId);
            createdOrdineId = null;
        }
    }

    // ── GET ordini ────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getOrdini_admin_200() throws Exception {
        mockMvc.perform(get("/api/ordini"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "CAMERIERE")
    void getOrdini_cameriere_200() throws Exception {
        mockMvc.perform(get("/api/ordini"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void getOrdini_cliente_403() throws Exception {
        mockMvc.perform(get("/api/ordini"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void getOrdini_nonAutenticato_401() throws Exception {
        mockMvc.perform(get("/api/ordini"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getOrdine_nonEsistente_404() throws Exception {
        mockMvc.perform(get("/api/ordini/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ── POST ordine ───────────────────────────────────────────────────────────

    @Test
    void createOrdine_adminJwt_201() throws Exception {
        Long tavoloId = tavoloRepository.findAll().get(0).getId();
        Long piattoId = piattoRepository.findAll().get(0).getId();
        String token = tokenAdmin();

        String body = String.format("""
                {
                  "tavoloId": %d,
                  "items": [{"piattoId": %d, "quantita": 2}]
                }
                """, tavoloId, piattoId);

        String response = mockMvc.perform(post("/api/ordini")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn().getResponse().getContentAsString();

        createdOrdineId = ((Number) JsonPath.read(response, "$.data.id")).longValue();
    }

    @Test
    @WithMockUser(roles = "CUOCO")
    void createOrdine_cuoco_403() throws Exception {
        String body = """
                {"tavoloId":1,"items":[{"piattoId":1,"quantita":1}]}
                """;

        mockMvc.perform(post("/api/ordini")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void createOrdine_senzaAuth_401() throws Exception {
        String body = """
                {"tavoloId":1,"items":[{"piattoId":1,"quantita":1}]}
                """;

        mockMvc.perform(post("/api/ordini")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    // ── PATCH stato ───────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void aggiornaStato_ordineNonEsistente_404() throws Exception {
        String body = """
                {"stato":"IN_PREPARAZIONE"}
                """;

        mockMvc.perform(patch("/api/ordini/999999/stato")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ── DELETE ordine ─────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "CAMERIERE")
    void deleteOrdine_cameriere_403() throws Exception {
        mockMvc.perform(delete("/api/ordini/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteOrdine_nonEsistente_404() throws Exception {
        mockMvc.perform(delete("/api/ordini/999999"))
                .andExpect(status().isNotFound());
    }
}
