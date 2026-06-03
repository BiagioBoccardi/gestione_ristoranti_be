package com.gestione.ristoranti.gestione_ristoranti.ricette;

import com.gestione.ristoranti.gestione_ristoranti.AbstractIntegrationTest;
import com.gestione.ristoranti.gestione_ristoranti.menu.repository.PiattoRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RicetteControllerTest extends AbstractIntegrationTest {

    @Autowired
    private PiattoRepository piattoRepository;

    private Long createdIngredienteId;

    @AfterEach
    void cleanup() {
        if (createdIngredienteId != null) {
            try {
                mockMvc.perform(delete("/api/ingredienti/" + createdIngredienteId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user("admin@restora.it").roles("ADMIN")));
            } catch (Exception ignored) {}
            createdIngredienteId = null;
        }
    }

    // ── GET /ingredienti ──────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllIngredienti_admin_200() throws Exception {
        mockMvc.perform(get("/api/ingredienti"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "CAMERIERE")
    void getAllIngredienti_cameriere_403() throws Exception {
        mockMvc.perform(get("/api/ingredienti"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void getAllIngredienti_nonAutenticato_401() throws Exception {
        mockMvc.perform(get("/api/ingredienti"))
                .andExpect(status().isUnauthorized());
    }

    // ── POST /ingredienti ─────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void creaIngrediente_admin_201() throws Exception {
        String body = """
                {"nome":"Pomodoro","unitaMisura":"KG","costoPerUnita":1.50}
                """;

        String response = mockMvc.perform(post("/api/ingredienti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.nome").value("Pomodoro"))
                .andReturn().getResponse().getContentAsString();

        createdIngredienteId = ((Number) JsonPath.read(response, "$.data.id")).longValue();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void creaIngrediente_campiMancanti_400() throws Exception {
        mockMvc.perform(post("/api/ingredienti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser(roles = "CUOCO")
    void creaIngrediente_cuoco_403() throws Exception {
        mockMvc.perform(post("/api/ingredienti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Olio\",\"unitaMisura\":\"L\",\"costoPerUnita\":5.00}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    // ── PUT /ingredienti/{id} ─────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void aggiornaIngrediente_nonEsistente_404() throws Exception {
        mockMvc.perform(put("/api/ingredienti/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Olio\",\"unitaMisura\":\"L\",\"costoPerUnita\":5.00}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ── DELETE /ingredienti/{id} ──────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminaIngrediente_nonEsistente_404() throws Exception {
        mockMvc.perform(delete("/api/ingredienti/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = "CAMERIERE")
    void eliminaIngrediente_cameriere_403() throws Exception {
        mockMvc.perform(delete("/api/ingredienti/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    // ── GET /ricette/{piattoId} ───────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRicetta_piattoEsistente_200() throws Exception {
        Long piattoId = piattoRepository.findAll().get(0).getId();
        mockMvc.perform(get("/api/ricette/" + piattoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRicetta_piattoNonEsistente_404() throws Exception {
        mockMvc.perform(get("/api/ricette/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = "CUOCO")
    void getRicetta_cuoco_403() throws Exception {
        mockMvc.perform(get("/api/ricette/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    // ── POST /ricette/{piattoId}/voci ─────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void aggiungiVoce_piattoNonEsistente_404() throws Exception {
        mockMvc.perform(post("/api/ricette/999999/voci")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ingredienteId\":1,\"quantita\":100,\"percentualeScarto\":0}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void aggiungiVoce_campiMancanti_400() throws Exception {
        Long piattoId = piattoRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/ricette/" + piattoId + "/voci")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
