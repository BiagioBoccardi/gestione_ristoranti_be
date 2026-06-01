package com.gestione.ristoranti.gestione_ristoranti.auth;

import com.gestione.ristoranti.gestione_ristoranti.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest extends AbstractIntegrationTest {

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    void login_credenzialivalide_200eToken() throws Exception {
        String body = """
                {"email":"admin@restora.it","password":"password123"}
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void login_passwordErrata_401() throws Exception {
        String body = """
                {"email":"admin@restora.it","password":"wrongpassword"}
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void login_emailMancante_400() throws Exception {
        String body = """
                {"password":"password123"}
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void login_jsonMalformato_400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{email: not-valid-json"))
                .andExpect(status().isBadRequest());
    }

    // ── Register ──────────────────────────────────────────────────────────────

    @Test
    void register_datiValidi_201() throws Exception {
        String email = "test-" + UUID.randomUUID() + "@test.it";
        String body = String.format("""
                {"nome":"Test User","email":"%s","password":"pass1234"}
                """, email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201));
    }

    @Test
    void register_emailDuplicata_409() throws Exception {
        String body = """
                {"nome":"Admin Dup","email":"admin@restora.it","password":"pass1234"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void register_campiMancanti_400() throws Exception {
        String body = """
                {"nome":"Solo Nome"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }
}
