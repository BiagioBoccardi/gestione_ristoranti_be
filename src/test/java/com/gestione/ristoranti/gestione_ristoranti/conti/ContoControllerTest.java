package com.gestione.ristoranti.gestione_ristoranti.conti;

import com.gestione.ristoranti.gestione_ristoranti.AbstractIntegrationTest;
import com.gestione.ristoranti.gestione_ristoranti.conti.repository.ContoRepository;
import com.gestione.ristoranti.gestione_ristoranti.menu.repository.PiattoRepository;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.OrdineRepository;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.TavoloRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ContoControllerTest extends AbstractIntegrationTest {

    @Autowired
    private TavoloRepository tavoloRepository;

    @Autowired
    private PiattoRepository piattoRepository;

    @Autowired
    private OrdineRepository ordineRepository;

    @Autowired
    private ContoRepository contoRepository;

    private Long ordineId;
    private Long contoId;

    @BeforeEach
    void setup() throws Exception {
        Long tavoloId = tavoloRepository.findAll().get(0).getId();
        Long piattoId = piattoRepository.findAll().get(0).getId();
        String token = tokenAdmin();

        String ordineBody = String.format("""
                {"tavoloId":%d,"items":[{"piattoId":%d,"quantita":2}]}
                """, tavoloId, piattoId);

        String ordineResponse = mockMvc.perform(post("/api/ordini")
                        .header("Authorization", authHeader(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ordineBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        ordineId = ((Number) JsonPath.read(ordineResponse, "$.data.id")).longValue();

        for (String stato : new String[]{"IN_PREPARAZIONE", "PRONTO", "CONSEGNATO"}) {
            mockMvc.perform(patch("/api/ordini/" + ordineId + "/stato")
                            .header("Authorization", authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nuovoStato\":\"" + stato + "\"}"))
                    .andExpect(status().isOk());
        }
    }

    @AfterEach
    void cleanup() {
        if (ordineId != null) {
            contoRepository.findByOrdineId(ordineId)
                    .ifPresent(c -> contoRepository.deleteById(c.getId()));
            ordineRepository.deleteById(ordineId);
            ordineId = null;
        }
        contoId = null;
    }

    // ── POST apriConto ────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "CAMERIERE")
    void apriConto_ordineEsistente_201() throws Exception {
        String response = mockMvc.perform(post("/api/conti/" + ordineId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn().getResponse().getContentAsString();

        contoId = ((Number) JsonPath.read(response, "$.data.id")).longValue();
    }

    @Test
    @WithMockUser(roles = "CUOCO")
    void apriConto_cuoco_403() throws Exception {
        mockMvc.perform(post("/api/conti/" + ordineId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = "CAMERIERE")
    void apriConto_ordineNonEsistente_404() throws Exception {
        mockMvc.perform(post("/api/conti/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ── GET conto ─────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "CAMERIERE")
    void getConto_dopoApertura_200() throws Exception {
        String apriResponse = mockMvc.perform(post("/api/conti/" + ordineId))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        contoId = ((Number) JsonPath.read(apriResponse, "$.data.id")).longValue();

        mockMvc.perform(get("/api/conti/" + ordineId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.totale").isNumber());
    }

    @Test
    @WithMockUser(roles = "CAMERIERE")
    void getConto_nonEsistente_404() throws Exception {
        mockMvc.perform(get("/api/conti/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ── PUT pagaConto ─────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void pagaConto_metodoPagamentoValido_200() throws Exception {
        String apriResponse = mockMvc.perform(post("/api/conti/" + ordineId))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        contoId = ((Number) JsonPath.read(apriResponse, "$.data.id")).longValue();

        String pagaBody = """
                {"metodo":"CONTANTI"}
                """;

        mockMvc.perform(put("/api/conti/" + contoId + "/paga")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pagaBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    // ── GET split ─────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "CAMERIERE")
    void split_duePersone_200() throws Exception {
        String apriResponse = mockMvc.perform(post("/api/conti/" + ordineId))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        contoId = ((Number) JsonPath.read(apriResponse, "$.data.id")).longValue();

        mockMvc.perform(get("/api/conti/" + contoId + "/split")
                        .param("persone", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quotaPerPersona").isNumber());
    }

    @Test
    void split_cliente_403() throws Exception {
        String apriResponse = mockMvc.perform(
                        post("/api/conti/" + ordineId)
                                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                        .user("admin@restora.it").roles("ADMIN")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        contoId = ((Number) JsonPath.read(apriResponse, "$.data.id")).longValue();

        mockMvc.perform(get("/api/conti/" + contoId + "/split")
                        .param("persone", "2")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user("cliente@test.com").roles("CLIENTE")))
                .andExpect(status().isForbidden());
    }
}
