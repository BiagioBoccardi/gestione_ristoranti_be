package com.gestione.ristoranti.gestione_ristoranti.menu;

import com.gestione.ristoranti.gestione_ristoranti.AbstractIntegrationTest;
import com.gestione.ristoranti.gestione_ristoranti.menu.repository.PiattoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MenuControllerTest extends AbstractIntegrationTest {

    @Autowired
    private PiattoRepository piattoRepository;

    private Long createdPiattoId;

    @AfterEach
    void cleanup() {
        if (createdPiattoId != null) {
            piattoRepository.deleteById(createdPiattoId);
            createdPiattoId = null;
        }
    }

    // ── GET piatti ────────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void getPiatti_autenticato_200() throws Exception {
        mockMvc.perform(get("/api/menu/piatti"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getPiatti_nonAutenticato_401() throws Exception {
        mockMvc.perform(get("/api/menu/piatti"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getPiatto_nonEsistente_404() throws Exception {
        mockMvc.perform(get("/api/menu/piatti/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ── POST piatto ───────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPiatto_admin_201() throws Exception {
        Long categoriaId = piattoRepository.findAll().get(0).getCategoria().getId();

        String body = String.format("""
                {"nome":"Piatto Test","descrizione":"Test","prezzo":9.90,"categoriaId":%d,"disponibile":true}
                """, categoriaId);

        String response = mockMvc.perform(post("/api/menu/piatti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.nome").value("Piatto Test"))
                .andReturn().getResponse().getContentAsString();

        createdPiattoId = com.jayway.jsonpath.JsonPath.read(response, "$.data.id");
    }

    @Test
    @WithMockUser(roles = "CAMERIERE")
    void createPiatto_cameriere_403() throws Exception {
        String body = """
                {"nome":"Piatto Vietato","descrizione":"Test","prezzo":5.00,"categoriaId":1,"disponibile":true}
                """;

        mockMvc.perform(post("/api/menu/piatti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void createPiatto_nonAutenticato_401() throws Exception {
        String body = """
                {"nome":"Piatto Anonimo","descrizione":"Test","prezzo":5.00,"categoriaId":1,"disponibile":true}
                """;

        mockMvc.perform(post("/api/menu/piatti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPiatto_senzaNome_400() throws Exception {
        String body = """
                {"descrizione":"Senza nome","prezzo":5.00,"categoriaId":1,"disponibile":true}
                """;

        mockMvc.perform(post("/api/menu/piatti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ── GET categorie ─────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void getCategorie_autenticato_200() throws Exception {
        mockMvc.perform(get("/api/menu/categorie"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser
    void getCategoria_nonEsistente_404() throws Exception {
        mockMvc.perform(get("/api/menu/categorie/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
