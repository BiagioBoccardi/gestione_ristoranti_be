package com.gestione.ristoranti.gestione_ristoranti.auth.controller;

import com.gestione.ristoranti.gestione_ristoranti.auth.api.AggiornaRuoloRequest;
import com.gestione.ristoranti.gestione_ristoranti.auth.api.CreaUtenteRequest;
import com.gestione.ristoranti.gestione_ristoranti.auth.api.RegisterResponse;
import com.gestione.ristoranti.gestione_ristoranti.auth.api.UtenteResponse;
import com.gestione.ristoranti.gestione_ristoranti.auth.service.AuthService;
import com.gestione.ristoranti.gestione_ristoranti.common.RestResponse;
import com.gestione.ristoranti.gestione_ristoranti.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Amministrazione", description = "Gestione utenti e ruoli — solo ADMIN")
public class AdminController {

    private final AuthService authService;

    public AdminController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Crea utente", description = "Crea un nuovo utente con ruolo specificato")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Utente creato"),
        @ApiResponse(responseCode = "400", description = "Dati non validi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email già in uso",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/crea-utente")
    public ResponseEntity<RestResponse<RegisterResponse>> creaUtente(@Valid @RequestBody CreaUtenteRequest request) {
        RegisterResponse response = authService.creaUtente(
                request.getNome(), request.getEmail(), request.getRuolo());
        return ResponseEntity.status(HttpStatus.CREATED).body(RestResponse.created(response));
    }

    @Operation(summary = "Lista utenti", description = "Restituisce tutti gli utenti registrati")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista utenti"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/utenti")
    public ResponseEntity<RestResponse<List<UtenteResponse>>> getUtenti() {
        return ResponseEntity.ok(RestResponse.ok(authService.getUtenti()));
    }

    @Operation(summary = "Dettaglio utente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Utente trovato"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Utente non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/utenti/{id}")
    public ResponseEntity<RestResponse<UtenteResponse>> getUtente(@PathVariable Long id) {
        return ResponseEntity.ok(RestResponse.ok(authService.getUtenteById(id)));
    }

    @Operation(summary = "Aggiorna ruolo utente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ruolo aggiornato"),
        @ApiResponse(responseCode = "400", description = "Ruolo non valido",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Utente non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/utenti/{id}/ruolo")
    public ResponseEntity<RestResponse<UtenteResponse>> aggiornaRuolo(
            @PathVariable Long id,
            @Valid @RequestBody AggiornaRuoloRequest request) {
        return ResponseEntity.ok(RestResponse.ok(authService.aggiornaRuolo(id, request.getRuolo())));
    }

    @Operation(summary = "Elimina utente")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Utente eliminato"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Utente non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/utenti/{id}")
    public ResponseEntity<Void> eliminaUtente(@PathVariable Long id) {
        authService.eliminaUtente(id);
        return ResponseEntity.noContent().build();
    }
}
