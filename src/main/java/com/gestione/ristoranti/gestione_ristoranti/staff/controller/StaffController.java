package com.gestione.ristoranti.gestione_ristoranti.staff.controller;

import com.gestione.ristoranti.gestione_ristoranti.common.RestResponse;
import com.gestione.ristoranti.gestione_ristoranti.exception.ErrorResponse;
import com.gestione.ristoranti.gestione_ristoranti.staff.dto.AggiornaStaffRequest;
import com.gestione.ristoranti.gestione_ristoranti.staff.dto.StaffDetailResponse;
import com.gestione.ristoranti.gestione_ristoranti.staff.dto.StaffResponse;
import com.gestione.ristoranti.gestione_ristoranti.staff.dto.TurnoRequest;
import com.gestione.ristoranti.gestione_ristoranti.staff.dto.TurnoResponse;
import com.gestione.ristoranti.gestione_ristoranti.staff.service.StaffService;
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
@RequestMapping("/api/staff")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Staff", description = "Gestione personale e turni — solo ADMIN")
public class StaffController {

    private final StaffService staffService;

    // ── Staff ─────────────────────────────────────────────────────────────────

    @Operation(summary = "Lista staff")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista staff"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<RestResponse<List<StaffResponse>>> getStaff() {
        return ResponseEntity.ok(RestResponse.ok(staffService.getStaff()));
    }

    @Operation(summary = "Dettaglio membro staff")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Staff trovato"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Staff non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<StaffDetailResponse>> getStaffById(@PathVariable Long id) {
        return ResponseEntity.ok(RestResponse.ok(staffService.getStaffById(id)));
    }

    @Operation(summary = "Aggiorna membro staff")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Staff aggiornato"),
        @ApiResponse(responseCode = "400", description = "Dati non validi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Staff non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<RestResponse<StaffResponse>> aggiornaStaff(
            @PathVariable Long id,
            @Valid @RequestBody AggiornaStaffRequest request) {
        return ResponseEntity.ok(RestResponse.ok(staffService.aggiornaStaff(id, request)));
    }

    @Operation(summary = "Elimina membro staff")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Staff eliminato"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Staff non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminaStaff(@PathVariable Long id) {
        staffService.eliminaStaff(id);
        return ResponseEntity.noContent().build();
    }

    // ── Turni ─────────────────────────────────────────────────────────────────

    @Operation(summary = "Lista tutti i turni")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista turni"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/turni")
    public ResponseEntity<RestResponse<List<TurnoResponse>>> getTurni() {
        return ResponseEntity.ok(RestResponse.ok(staffService.getTurni()));
    }

    @Operation(summary = "Turni di un membro staff")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista turni"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Staff non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/turni")
    public ResponseEntity<RestResponse<List<TurnoResponse>>> getTurniByUtente(@PathVariable Long id) {
        return ResponseEntity.ok(RestResponse.ok(staffService.getTurniByUtente(id)));
    }

    @Operation(summary = "Crea turno")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Turno creato"),
        @ApiResponse(responseCode = "400", description = "Dati non validi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/turni")
    public ResponseEntity<RestResponse<TurnoResponse>> creaTurno(@Valid @RequestBody TurnoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(RestResponse.created(staffService.creaTurno(request)));
    }

    @Operation(summary = "Aggiorna turno")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Turno aggiornato"),
        @ApiResponse(responseCode = "400", description = "Dati non validi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Turno non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/turni/{id}")
    public ResponseEntity<RestResponse<TurnoResponse>> aggiornaTurno(
            @PathVariable Long id,
            @Valid @RequestBody TurnoRequest request) {
        return ResponseEntity.ok(RestResponse.ok(staffService.aggiornaTurno(id, request)));
    }

    @Operation(summary = "Elimina turno")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Turno eliminato"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Turno non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/turni/{id}")
    public ResponseEntity<Void> eliminaTurno(@PathVariable Long id) {
        staffService.eliminaTurno(id);
        return ResponseEntity.noContent().build();
    }
}
