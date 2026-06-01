package com.gestione.ristoranti.gestione_ristoranti.auth.controller;

import com.gestione.ristoranti.gestione_ristoranti.auth.api.ForgotPasswordRequest;
import com.gestione.ristoranti.gestione_ristoranti.auth.api.LoginRequest;
import com.gestione.ristoranti.gestione_ristoranti.auth.api.LoginResponse;
import com.gestione.ristoranti.gestione_ristoranti.auth.api.RegisterRequest;
import com.gestione.ristoranti.gestione_ristoranti.auth.api.RegisterResponse;
import com.gestione.ristoranti.gestione_ristoranti.auth.api.ResetPasswordRequest;
import com.gestione.ristoranti.gestione_ristoranti.auth.service.AuthService;
import com.gestione.ristoranti.gestione_ristoranti.auth.service.PasswordResetService;
import com.gestione.ristoranti.gestione_ristoranti.common.RestResponse;
import com.gestione.ristoranti.gestione_ristoranti.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticazione", description = "Login e registrazione utenti")
@SecurityRequirements
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @Operation(summary = "Login utente", description = "Autentica un utente e restituisce il token JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login effettuato con successo"),
        @ApiResponse(responseCode = "400", description = "Dati non validi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Credenziali errate",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<RestResponse<LoginResponse>> authenticate(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(RestResponse.ok(authService.authenticate(request.getEmail(), request.getPassword())));
    }

    @Operation(summary = "Registrazione utente", description = "Registra un nuovo utente con ruolo CLIENTE")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Utente registrato con successo"),
        @ApiResponse(responseCode = "400", description = "Dati non validi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email già in uso",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<RestResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestResponse.created(authService.register(request.getNome(), request.getEmail(), request.getPassword())));
    }

    @Operation(summary = "Richiedi reset password",
               description = "Invia un link di reset all'email indicata (risposta identica indipendentemente dall'esistenza dell'account)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Richiesta elaborata"),
        @ApiResponse(responseCode = "400", description = "Email non valida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<RestResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.richiediReset(request.getEmail());
        return ResponseEntity.ok(RestResponse.ok(
                "Se l'email è registrata, riceverai un link per reimpostare la password."));
    }

    @Operation(summary = "Reimposta password", description = "Valida il token e aggiorna la password dell'utente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password aggiornata"),
        @ApiResponse(responseCode = "400", description = "Token non valido, scaduto o già usato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/reset-password")
    public ResponseEntity<RestResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNuovaPassword());
        return ResponseEntity.ok(RestResponse.ok("Password reimpostata con successo."));
    }
}
