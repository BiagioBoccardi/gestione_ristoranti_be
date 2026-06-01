package com.gestione.ristoranti.gestione_ristoranti.ordini.controller;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.StatoTavolo;
import com.gestione.ristoranti.gestione_ristoranti.common.RestResponse;
import com.gestione.ristoranti.gestione_ristoranti.exception.ErrorResponse;
import com.gestione.ristoranti.gestione_ristoranti.ordini.dto.TavoloRequest;
import com.gestione.ristoranti.gestione_ristoranti.ordini.dto.TavoloResponse;
import com.gestione.ristoranti.gestione_ristoranti.ordini.service.TavoloService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tavoli")
@Tag(name = "Tavoli", description = "Gestione tavoli del ristorante")
public class TavoloController {

    private final TavoloService tavoloService;

    public TavoloController(TavoloService tavoloService) {
        this.tavoloService = tavoloService;
    }

    @Operation(summary = "Lista tavoli", description = "Restituisce tutti i tavoli, con filtro opzionale per stato")
    @ApiResponse(responseCode = "200", description = "Lista tavoli")
    @GetMapping
    public ResponseEntity<RestResponse<List<TavoloResponse>>> getTavoli(
            @RequestParam(required = false) StatoTavolo stato) {
        return ResponseEntity.ok(RestResponse.ok(tavoloService.getAll(stato)));
    }

    @Operation(summary = "Dettaglio tavolo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tavolo trovato"),
        @ApiResponse(responseCode = "404", description = "Tavolo non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<TavoloResponse>> getTavolo(@PathVariable Long id) {
        return ResponseEntity.ok(RestResponse.ok(tavoloService.getById(id)));
    }

    @Operation(summary = "Crea tavolo", description = "Solo ADMIN")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tavolo creato"),
        @ApiResponse(responseCode = "400", description = "Dati non validi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RestResponse<TavoloResponse>> createTavolo(@Valid @RequestBody TavoloRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(RestResponse.created(tavoloService.create(request)));
    }

    @Operation(summary = "Aggiorna tavolo", description = "Solo ADMIN")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tavolo aggiornato"),
        @ApiResponse(responseCode = "400", description = "Dati non validi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Tavolo non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RestResponse<TavoloResponse>> updateTavolo(@PathVariable Long id,
                                                                     @Valid @RequestBody TavoloRequest request) {
        return ResponseEntity.ok(RestResponse.ok(tavoloService.update(id, request)));
    }

    @Operation(summary = "Cambia stato tavolo", description = "Aggiorna lo stato del tavolo (LIBERO, OCCUPATO, IN_ATTESA_CONTO)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Stato aggiornato"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Tavolo non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/stato")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAMERIERE')")
    public ResponseEntity<RestResponse<TavoloResponse>> aggiornaStato(@PathVariable Long id,
                                                                      @RequestParam StatoTavolo nuovoStato) {
        return ResponseEntity.ok(RestResponse.ok(tavoloService.aggiornaStato(id, nuovoStato)));
    }

    @Operation(summary = "Elimina tavolo", description = "Solo ADMIN")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Tavolo eliminato"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Tavolo non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTavolo(@PathVariable Long id) {
        tavoloService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
