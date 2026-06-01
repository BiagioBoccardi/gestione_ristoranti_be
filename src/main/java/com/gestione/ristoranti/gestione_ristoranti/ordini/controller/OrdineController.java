package com.gestione.ristoranti.gestione_ristoranti.ordini.controller;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.StatoOrdine;
import com.gestione.ristoranti.gestione_ristoranti.auth.security.UtenteDetails;
import com.gestione.ristoranti.gestione_ristoranti.common.RestResponse;
import com.gestione.ristoranti.gestione_ristoranti.exception.ErrorResponse;
import com.gestione.ristoranti.gestione_ristoranti.ordini.dto.CambioStatoRequest;
import com.gestione.ristoranti.gestione_ristoranti.ordini.dto.OrdineRequest;
import com.gestione.ristoranti.gestione_ristoranti.ordini.dto.OrdineResponse;
import com.gestione.ristoranti.gestione_ristoranti.ordini.dto.OrdineUpdateRequest;
import com.gestione.ristoranti.gestione_ristoranti.ordini.service.OrdineService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordini")
@Tag(name = "Ordini", description = "Gestione ordini del ristorante")
public class OrdineController {

    private final OrdineService ordineService;

    public OrdineController(OrdineService ordineService) {
        this.ordineService = ordineService;
    }

    @Operation(summary = "Lista ordini", description = "Restituisce tutti gli ordini, con filtro opzionale per stato")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista ordini"),
        @ApiResponse(responseCode = "401", description = "Non autenticato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CAMERIERE', 'CUOCO')")
    public ResponseEntity<RestResponse<List<OrdineResponse>>> getOrdini(
            @RequestParam(required = false) StatoOrdine stato) {
        return ResponseEntity.ok(RestResponse.ok(ordineService.getAll(stato)));
    }

    @Operation(summary = "Dettaglio ordine")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ordine trovato"),
        @ApiResponse(responseCode = "401", description = "Non autenticato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Ordine non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<OrdineResponse>> getOrdine(@PathVariable Long id) {
        return ResponseEntity.ok(RestResponse.ok(ordineService.getById(id)));
    }

    @Operation(summary = "Ordini per tavolo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista ordini del tavolo"),
        @ApiResponse(responseCode = "401", description = "Non autenticato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/tavolo/{tavoloId}")
    public ResponseEntity<RestResponse<List<OrdineResponse>>> getOrdiniByTavolo(@PathVariable Long tavoloId) {
        return ResponseEntity.ok(RestResponse.ok(ordineService.getByTavolo(tavoloId)));
    }

    @Operation(summary = "Crea ordine", description = "Crea un nuovo ordine associato a un tavolo. Solo ADMIN e CAMERIERE")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Ordine creato"),
        @ApiResponse(responseCode = "400", description = "Dati non validi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Tavolo o piatto non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CAMERIERE')")
    public ResponseEntity<RestResponse<OrdineResponse>> createOrdine(
            @Valid @RequestBody OrdineRequest request,
            @AuthenticationPrincipal UtenteDetails utenteDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestResponse.created(ordineService.create(request, utenteDetails.getId())));
    }

    @Operation(summary = "Aggiorna ordine", description = "Modifica gli item di un ordine esistente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ordine aggiornato"),
        @ApiResponse(responseCode = "400", description = "Dati non validi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Ordine non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAMERIERE')")
    public ResponseEntity<RestResponse<OrdineResponse>> updateOrdine(@PathVariable Long id,
                                                                     @Valid @RequestBody OrdineUpdateRequest request) {
        return ResponseEntity.ok(RestResponse.ok(ordineService.update(id, request)));
    }

    @Operation(summary = "Cambia stato ordine", description = "Aggiorna lo stato dell'ordine (es. IN_PREPARAZIONE, PRONTO, CONSEGNATO)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Stato aggiornato"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Ordine non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Transizione di stato non valida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/stato")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAMERIERE', 'CUOCO')")
    public ResponseEntity<RestResponse<OrdineResponse>> aggiornaStato(@PathVariable Long id,
                                                                      @Valid @RequestBody CambioStatoRequest request) {
        return ResponseEntity.ok(RestResponse.ok(ordineService.aggiornaStato(id, request)));
    }

    @Operation(summary = "Elimina ordine", description = "Solo ADMIN")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Ordine eliminato"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Ordine non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrdine(@PathVariable Long id) {
        ordineService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
