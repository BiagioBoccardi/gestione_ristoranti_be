package com.gestione.ristoranti.gestione_ristoranti.ricette.controller;

import com.gestione.ristoranti.gestione_ristoranti.common.RestResponse;
import com.gestione.ristoranti.gestione_ristoranti.exception.ErrorResponse;
import com.gestione.ristoranti.gestione_ristoranti.ricette.dto.IngredienteRequest;
import com.gestione.ristoranti.gestione_ristoranti.ricette.dto.IngredienteResponse;
import com.gestione.ristoranti.gestione_ristoranti.ricette.dto.RicettaResponse;
import com.gestione.ristoranti.gestione_ristoranti.ricette.dto.RicettaVoceRequest;
import com.gestione.ristoranti.gestione_ristoranti.ricette.service.RicetteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Ricette", description = "Gestione ingredienti e ricette dei piatti — solo ADMIN")
public class RicetteController {

    private final RicetteService ricetteService;

    // ── Ingredienti ──────────────────────────────────────────────────────────

    @Operation(summary = "Lista ingredienti", description = "Restituisce tutti gli ingredienti disponibili")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista ingredienti"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/ingredienti")
    public ResponseEntity<RestResponse<List<IngredienteResponse>>> getAllIngredienti() {
        return ResponseEntity.ok(RestResponse.ok(ricetteService.getAllIngredienti()));
    }

    @Operation(summary = "Crea ingrediente")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Ingrediente creato"),
        @ApiResponse(responseCode = "400", description = "Dati non validi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/ingredienti")
    public ResponseEntity<RestResponse<IngredienteResponse>> creaIngrediente(
            @Valid @RequestBody IngredienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestResponse.created(ricetteService.creaIngrediente(request)));
    }

    @Operation(summary = "Aggiorna ingrediente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ingrediente aggiornato"),
        @ApiResponse(responseCode = "400", description = "Dati non validi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Ingrediente non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/ingredienti/{id}")
    public ResponseEntity<RestResponse<IngredienteResponse>> aggiornaIngrediente(
            @PathVariable Long id,
            @Valid @RequestBody IngredienteRequest request) {
        return ResponseEntity.ok(RestResponse.ok(ricetteService.aggiornaIngrediente(id, request)));
    }

    @Operation(summary = "Elimina ingrediente")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Ingrediente eliminato"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Ingrediente non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/ingredienti/{id}")
    public ResponseEntity<Void> eliminaIngrediente(@PathVariable Long id) {
        ricetteService.eliminaIngrediente(id);
        return ResponseEntity.noContent().build();
    }

    // ── Ricette ───────────────────────────────────────────────────────────────

    @Operation(summary = "Ricetta di un piatto", description = "Restituisce la ricetta con ingredienti e food cost")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ricetta trovata"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Piatto non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/ricette/{piattoId}")
    public ResponseEntity<RestResponse<RicettaResponse>> getRicetta(@PathVariable Long piattoId) {
        return ResponseEntity.ok(RestResponse.ok(ricetteService.getRicetta(piattoId)));
    }

    @Operation(summary = "Aggiungi voce ricetta", description = "Aggiunge un ingrediente alla ricetta del piatto")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Voce aggiunta"),
        @ApiResponse(responseCode = "400", description = "Dati non validi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Piatto o ingrediente non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/ricette/{piattoId}/voci")
    public ResponseEntity<RestResponse<RicettaResponse>> aggiungiVoce(
            @PathVariable Long piattoId,
            @Valid @RequestBody RicettaVoceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestResponse.created(ricetteService.aggiungiVoce(piattoId, request)));
    }

    @Operation(summary = "Elimina voce ricetta", description = "Rimuove un ingrediente dalla ricetta del piatto")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Voce eliminata"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Piatto o voce non trovata",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/ricette/{piattoId}/voci/{voceId}")
    public ResponseEntity<Void> eliminaVoce(
            @PathVariable Long piattoId,
            @PathVariable Long voceId) {
        ricetteService.eliminaVoce(piattoId, voceId);
        return ResponseEntity.noContent().build();
    }
}
