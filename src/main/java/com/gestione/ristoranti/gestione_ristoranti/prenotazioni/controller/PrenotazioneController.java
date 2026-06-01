package com.gestione.ristoranti.gestione_ristoranti.prenotazioni.controller;

import com.gestione.ristoranti.gestione_ristoranti.common.RestResponse;
import com.gestione.ristoranti.gestione_ristoranti.exception.ErrorResponse;
import com.gestione.ristoranti.gestione_ristoranti.prenotazioni.dto.PrenotazioneRequest;
import com.gestione.ristoranti.gestione_ristoranti.prenotazioni.dto.PrenotazioneResponse;
import com.gestione.ristoranti.gestione_ristoranti.prenotazioni.service.PrenotazioneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/prenotazioni")
@Tag(name = "Prenotazioni", description = "Gestione prenotazioni tavoli")
public class PrenotazioneController {

    private final PrenotazioneService prenotazioneService;

    public PrenotazioneController(PrenotazioneService prenotazioneService) {
        this.prenotazioneService = prenotazioneService;
    }

    @Operation(summary = "Crea prenotazione", description = "Accessibile da qualsiasi utente autenticato")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Prenotazione creata"),
        @ApiResponse(responseCode = "400", description = "Dati non validi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Non autenticato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Tavolo non disponibile nella fascia oraria",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<RestResponse<PrenotazioneResponse>> crea(
            @Valid @RequestBody PrenotazioneRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestResponse.created(prenotazioneService.crea(request, userDetails.getUsername())));
    }

    @Operation(summary = "Dettaglio prenotazione", description = "Accessibile da ADMIN e CAMERIERE")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Prenotazione trovata"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Prenotazione non trovata",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAMERIERE')")
    public ResponseEntity<RestResponse<PrenotazioneResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(RestResponse.ok(prenotazioneService.getById(id)));
    }

    @Operation(summary = "Le mie prenotazioni", description = "Restituisce le prenotazioni dell'utente autenticato")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista prenotazioni"),
        @ApiResponse(responseCode = "401", description = "Non autenticato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/mie")
    public ResponseEntity<RestResponse<List<PrenotazioneResponse>>> getMie(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(RestResponse.ok(prenotazioneService.getMiePrenotazioni(userDetails.getUsername())));
    }

    @Operation(summary = "Prenotazioni per data", description = "Solo ADMIN e CAMERIERE")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista prenotazioni del giorno"),
        @ApiResponse(responseCode = "400", description = "Data non valida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CAMERIERE')")
    public ResponseEntity<RestResponse<List<PrenotazioneResponse>>> getPerData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(RestResponse.ok(prenotazioneService.getPrenotazioniPerData(data)));
    }

    @Operation(summary = "Modifica prenotazione", description = "Il cliente può modificare solo le proprie prenotazioni")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Prenotazione aggiornata"),
        @ApiResponse(responseCode = "400", description = "Dati non validi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Prenotazione non trovata",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Tavolo non disponibile",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<RestResponse<PrenotazioneResponse>> modifica(
            @PathVariable Long id,
            @Valid @RequestBody PrenotazioneRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(RestResponse.ok(prenotazioneService.modifica(id, request, userDetails.getUsername())));
    }

    @Operation(summary = "Cancella prenotazione", description = "Il cliente può cancellare solo le proprie prenotazioni")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Prenotazione cancellata"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Prenotazione non trovata",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancella(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        prenotazioneService.cancella(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
