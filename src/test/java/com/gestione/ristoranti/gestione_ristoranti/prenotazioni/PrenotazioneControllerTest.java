package com.gestione.ristoranti.gestione_ristoranti.prenotazioni;

import com.gestione.ristoranti.gestione_ristoranti.AbstractIntegrationTest;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.TavoloRepository;
import com.gestione.ristoranti.gestione_ristoranti.prenotazioni.repository.PrenotazioneRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PrenotazioneControllerTest extends AbstractIntegrationTest {

    @Autowired
    private TavoloRepository tavoloRepository;

    @Autowired
    private PrenotazioneRepository prenotazioneRepository;

    private Long createdPrenotazioneId;

    @AfterEach
    void cleanup() {
        if (createdPrenotazioneId != null) {
            prenotazioneRepository.deleteById(createdPrenotazioneId);
            createdPrenotazioneId = null;
        }
    }

    private String bodyPrenotazione(Long tavoloId, String data, String ora) {
        return String.format("""
                {"tavoloId":%d,"data":"%s","ora":"%s","coperti":2,"note":"Test"}
                """, tavoloId, data, ora);
    }

    private Long creaPrenotazione(Long tavoloId, String data, String ora) throws Exception {
        String body = bodyPrenotazione(tavoloId, data, ora);
        String response = mockMvc.perform(post("/api/prenotazioni")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user("admin@restora.it").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.data.id")).longValue();
    }

    // ── POST prenotazione ─────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin@restora.it", roles = "ADMIN")
    void creaPrenotazione_datiValidi_201() throws Exception {
        Long tavoloId = tavoloRepository.findAll().get(0).getId();
        String body = bodyPrenotazione(tavoloId, LocalDate.now().plusDays(5).toString(), "20:00:00");

        String response = mockMvc.perform(post("/api/prenotazioni")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn().getResponse().getContentAsString();

        createdPrenotazioneId = ((Number) JsonPath.read(response, "$.data.id")).longValue();
    }

    @Test
    void creaPrenotazione_nonAutenticato_401() throws Exception {
        String body = """
                {"tavoloId":1,"data":"2099-12-01","ora":"20:00:00","coperti":2}
                """;
        mockMvc.perform(post("/api/prenotazioni")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@restora.it", roles = "ADMIN")
    void creaPrenotazione_campiMancanti_400() throws Exception {
        mockMvc.perform(post("/api/prenotazioni")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tavoloId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ── PUT modifica prenotazione ─────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin@restora.it", roles = "ADMIN")
    void modificaPrenotazione_datiValidi_200() throws Exception {
        Long tavoloId = tavoloRepository.findAll().get(0).getId();
        createdPrenotazioneId = creaPrenotazione(tavoloId, LocalDate.now().plusDays(10).toString(), "19:00:00");

        String bodyModifica = bodyPrenotazione(tavoloId, LocalDate.now().plusDays(11).toString(), "21:00:00");

        mockMvc.perform(put("/api/prenotazioni/" + createdPrenotazioneId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyModifica))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(createdPrenotazioneId));
    }

    @Test
    @WithMockUser(username = "admin@restora.it", roles = "ADMIN")
    void modificaPrenotazione_nonEsistente_404() throws Exception {
        Long tavoloId = tavoloRepository.findAll().get(0).getId();
        String body = bodyPrenotazione(tavoloId, LocalDate.now().plusDays(1).toString(), "20:00:00");

        mockMvc.perform(put("/api/prenotazioni/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void modificaPrenotazione_nonAutenticato_401() throws Exception {
        mockMvc.perform(put("/api/prenotazioni/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tavoloId\":1,\"data\":\"2099-01-01\",\"ora\":\"20:00:00\",\"coperti\":2}"))
                .andExpect(status().isUnauthorized());
    }

    // ── DELETE cancella prenotazione ──────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin@restora.it", roles = "ADMIN")
    void cancellaPrenotazione_propriaPrenotazione_204() throws Exception {
        Long tavoloId = tavoloRepository.findAll().get(0).getId();
        Long id = creaPrenotazione(tavoloId, LocalDate.now().plusDays(20).toString(), "20:00:00");

        mockMvc.perform(delete("/api/prenotazioni/" + id))
                .andExpect(status().isNoContent());

        createdPrenotazioneId = null;
    }

    @Test
    @WithMockUser(username = "admin@restora.it", roles = "ADMIN")
    void cancellaPrenotazione_nonEsistente_404() throws Exception {
        mockMvc.perform(delete("/api/prenotazioni/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void cancellaPrenotazione_nonAutenticato_401() throws Exception {
        mockMvc.perform(delete("/api/prenotazioni/1"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET ───────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin@restora.it", roles = "ADMIN")
    void getMiePrenotazioni_autenticato_200() throws Exception {
        mockMvc.perform(get("/api/prenotazioni/mie"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getMiePrenotazioni_nonAutenticato_401() throws Exception {
        mockMvc.perform(get("/api/prenotazioni/mie"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CAMERIERE")
    void getPerData_cameriere_200() throws Exception {
        mockMvc.perform(get("/api/prenotazioni").param("data", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void getPerData_cliente_403() throws Exception {
        mockMvc.perform(get("/api/prenotazioni").param("data", LocalDate.now().toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(roles = "CAMERIERE")
    void getById_nonEsistente_404() throws Exception {
        mockMvc.perform(get("/api/prenotazioni/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void getById_cliente_403() throws Exception {
        mockMvc.perform(get("/api/prenotazioni/1"))
                .andExpect(status().isForbidden());
    }
}
