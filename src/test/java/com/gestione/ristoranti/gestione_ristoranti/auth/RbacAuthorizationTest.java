package com.gestione.ristoranti.gestione_ristoranti.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RbacAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "STAFF")
    public void quandoStaffAccedeAdAreaAdmin_alloraForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/utenti"))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void quandoAdminAccedeAdAreaAdmin_alloraOk() throws Exception {
        mockMvc.perform(get("/api/admin/utenti"))
               .andExpect(status().isOk());
    }

    @Test
    public void quandoAnonimoAccede_allora401() throws Exception {
        mockMvc.perform(get("/api/admin/utenti"))
               .andExpect(status().isUnauthorized());
    }
}
