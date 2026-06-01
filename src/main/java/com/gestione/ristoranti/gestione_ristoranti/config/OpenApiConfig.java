package com.gestione.ristoranti.gestione_ristoranti.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Gestione Ristorante API")
                        .version("1.0.0")
                        .description("API REST per la gestione di ristoranti: menu, ordini, tavoli, prenotazioni, conto e analytics.")
                        .contact(new Contact()
                                .name("Infobasic")
                                .email("admin@restora.it")))
                .servers(List.of(
                        new Server().url("http://localhost").description("Produzione (nginx)"),
                        new Server().url("http://localhost:8080").description("Sviluppo locale")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Inserire il token JWT ottenuto da POST /api/auth/login")));
    }
}
