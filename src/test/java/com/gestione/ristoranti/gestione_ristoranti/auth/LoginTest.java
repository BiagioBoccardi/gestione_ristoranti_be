package com.gestione.ristoranti.gestione_ristoranti.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

// IMPORT STATICI: Questi risolvono gli errori "method undefined"
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // Carica il contesto completo dell'applicazione
@AutoConfigureMockMvc // Configura automaticamente MockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class LoginTest {

    @Autowired
    private MockMvc mockMvc; // Risolve l'errore "mockMvc cannot be resolved"

    @Test
    @WithMockUser(roles = "STAFF")
    public void quandoStaffAccedeAdAreaAdmin_alloraForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/staff-list"))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void quandoAdminAccedeAdAreaAdmin_alloraOk() throws Exception {
        mockMvc.perform(get("/api/admin/staff-list"))
               .andExpect(status().isOk());
    }
}