package com.gestione.ristoranti.gestione_ristoranti.menu.controller;

import com.gestione.ristoranti.gestione_ristoranti.common.RestResponse;
import com.gestione.ristoranti.gestione_ristoranti.exception.ErrorResponse;
import com.gestione.ristoranti.gestione_ristoranti.menu.dto.CategoriaRequest;
import com.gestione.ristoranti.gestione_ristoranti.menu.dto.CategoriaResponse;
import com.gestione.ristoranti.gestione_ristoranti.menu.dto.PiattoRequest;
import com.gestione.ristoranti.gestione_ristoranti.menu.dto.PiattoResponse;
import com.gestione.ristoranti.gestione_ristoranti.menu.service.CategoriaService;
import com.gestione.ristoranti.gestione_ristoranti.menu.service.PiattoService;
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
@RequestMapping("/api/menu")
@Tag(name = "Menu", description = "Gestione piatti e categorie del menu")
public class MenuController {

    private final PiattoService piattoService;
    private final CategoriaService categoriaService;

    public MenuController(PiattoService piattoService, CategoriaService categoriaService) {
        this.piattoService = piattoService;
        this.categoriaService = categoriaService;
    }

    // ---- Piatti ----

    @Operation(summary = "Lista piatti", description = "Restituisce tutti i piatti, con filtri opzionali per categoria e disponibilità")
    @ApiResponse(responseCode = "200", description = "Lista piatti")
    @GetMapping("/piatti")
    public ResponseEntity<RestResponse<List<PiattoResponse>>> getPiatti(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Boolean disponibile) {
        return ResponseEntity.ok(RestResponse.ok(piattoService.getAll(categoriaId, disponibile)));
    }

    @Operation(summary = "Dettaglio piatto")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Piatto trovato"),
        @ApiResponse(responseCode = "404", description = "Piatto non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/piatti/{id}")
    public ResponseEntity<RestResponse<PiattoResponse>> getPiatto(@PathVariable Long id) {
        return ResponseEntity.ok(RestResponse.ok(piattoService.getById(id)));
    }

    @Operation(summary = "Crea piatto", description = "Solo ADMIN")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Piatto creato"),
        @ApiResponse(responseCode = "400", description = "Dati non validi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/piatti")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RestResponse<PiattoResponse>> createPiatto(@Valid @RequestBody PiattoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(RestResponse.created(piattoService.create(request)));
    }

    @Operation(summary = "Aggiorna piatto", description = "Solo ADMIN")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Piatto aggiornato"),
        @ApiResponse(responseCode = "400", description = "Dati non validi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Piatto non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/piatti/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RestResponse<PiattoResponse>> updatePiatto(@PathVariable Long id,
                                                                     @Valid @RequestBody PiattoRequest request) {
        return ResponseEntity.ok(RestResponse.ok(piattoService.update(id, request)));
    }

    @Operation(summary = "Cambia disponibilità piatto", description = "ADMIN o CUOCO")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Disponibilità aggiornata"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Piatto non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/piatti/{id}/disponibilita")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUOCO')")
    public ResponseEntity<Void> toggleDisponibilita(@PathVariable Long id) {
        piattoService.toggleDisponibilita(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Elimina piatto", description = "Solo ADMIN")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Piatto eliminato"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Piatto non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/piatti/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePiatto(@PathVariable Long id) {
        piattoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Categorie ----

    @Operation(summary = "Lista categorie")
    @ApiResponse(responseCode = "200", description = "Lista categorie")
    @GetMapping("/categorie")
    public ResponseEntity<RestResponse<List<CategoriaResponse>>> getCategorie() {
        return ResponseEntity.ok(RestResponse.ok(categoriaService.getAll()));
    }

    @Operation(summary = "Dettaglio categoria")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoria trovata"),
        @ApiResponse(responseCode = "404", description = "Categoria non trovata",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/categorie/{id}")
    public ResponseEntity<RestResponse<CategoriaResponse>> getCategoria(@PathVariable Long id) {
        return ResponseEntity.ok(RestResponse.ok(categoriaService.getById(id)));
    }

    @Operation(summary = "Crea categoria", description = "Solo ADMIN")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Categoria creata"),
        @ApiResponse(responseCode = "400", description = "Dati non validi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/categorie")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RestResponse<CategoriaResponse>> createCategoria(@Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(RestResponse.created(categoriaService.create(request)));
    }

    @Operation(summary = "Aggiorna categoria", description = "Solo ADMIN")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoria aggiornata"),
        @ApiResponse(responseCode = "400", description = "Dati non validi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Categoria non trovata",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/categorie/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RestResponse<CategoriaResponse>> updateCategoria(@PathVariable Long id,
                                                                           @Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.ok(RestResponse.ok(categoriaService.update(id, request)));
    }

    @Operation(summary = "Elimina categoria", description = "Solo ADMIN")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Categoria eliminata"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Categoria non trovata",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/categorie/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategoria(@PathVariable Long id) {
        categoriaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
