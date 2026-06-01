package com.gestione.ristoranti.gestione_ristoranti.conti.controller;

import com.gestione.ristoranti.gestione_ristoranti.common.RestResponse;
import com.gestione.ristoranti.gestione_ristoranti.conti.dto.ContoResponse;
import com.gestione.ristoranti.gestione_ristoranti.conti.dto.PagaContoRequest;
import com.gestione.ristoranti.gestione_ristoranti.conti.dto.SplitBillResponse;
import com.gestione.ristoranti.gestione_ristoranti.conti.service.ContoService;
import com.gestione.ristoranti.gestione_ristoranti.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/conti")
@RequiredArgsConstructor
@Tag(name = "Conti", description = "Gestione conti, pagamenti ed export")
public class ContoController {

    private final ContoService contoService;

    @Operation(summary = "Apri conto", description = "Crea il conto per un ordine. Solo ADMIN e CAMERIERE")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Conto aperto"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Ordine non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Conto già aperto per questo ordine",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{ordineId}")
    @PreAuthorize("hasAnyRole('ADMIN','CAMERIERE')")
    public ResponseEntity<RestResponse<ContoResponse>> apriConto(@PathVariable Long ordineId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(RestResponse.created(contoService.apriConto(ordineId)));
    }

    @Operation(summary = "Visualizza conto")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Conto trovato"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Conto non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{ordineId}")
    @PreAuthorize("hasAnyRole('ADMIN','CAMERIERE')")
    public ResponseEntity<RestResponse<ContoResponse>> getConto(@PathVariable Long ordineId) {
        return ResponseEntity.ok(RestResponse.ok(contoService.getConto(ordineId)));
    }

    @Operation(summary = "Registra pagamento")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pagamento registrato"),
        @ApiResponse(responseCode = "400", description = "Dati pagamento non validi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Conto non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{contoId}/paga")
    @PreAuthorize("hasAnyRole('ADMIN','CAMERIERE')")
    public ResponseEntity<RestResponse<ContoResponse>> pagaConto(
            @PathVariable Long contoId,
            @Valid @RequestBody PagaContoRequest request) {
        return ResponseEntity.ok(RestResponse.ok(contoService.pagaConto(contoId, request)));
    }

    @Operation(summary = "Calcola split bill", description = "Divide il conto tra N persone")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Split calcolato"),
        @ApiResponse(responseCode = "400", description = "Numero persone non valido",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Conto non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{contoId}/split")
    @PreAuthorize("hasAnyRole('ADMIN','CAMERIERE')")
    public ResponseEntity<RestResponse<SplitBillResponse>> split(
            @PathVariable Long contoId,
            @RequestParam int persone) {
        return ResponseEntity.ok(RestResponse.ok(contoService.calcolaSplit(contoId, persone)));
    }

    @Operation(summary = "Export PDF conto", description = "Scarica il conto in formato PDF")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF generato"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Conto non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{contoId}/export/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','CAMERIERE')")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long contoId) throws IOException {
        byte[] pdf = contoService.exportPdf(contoId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"conto-" + contoId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @Operation(summary = "Export Excel conto", description = "Scarica il conto in formato Excel")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Excel generato"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Conto non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{contoId}/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN','CAMERIERE')")
    public ResponseEntity<byte[]> exportExcel(@PathVariable Long contoId) throws IOException {
        byte[] excel = contoService.exportExcel(contoId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"conto-" + contoId + ".xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}
