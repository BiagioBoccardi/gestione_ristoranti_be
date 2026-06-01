package com.gestione.ristoranti.gestione_ristoranti.analytics.controller;

import com.gestione.ristoranti.gestione_ristoranti.analytics.dto.FoodCostResponse;
import com.gestione.ristoranti.gestione_ristoranti.analytics.dto.KpiResponse;
import com.gestione.ristoranti.gestione_ristoranti.analytics.dto.MetodoPagamentoStatResponse;
import com.gestione.ristoranti.gestione_ristoranti.analytics.dto.RevenuePointResponse;
import com.gestione.ristoranti.gestione_ristoranti.analytics.dto.TopPiattoResponse;
import com.gestione.ristoranti.gestione_ristoranti.analytics.service.AnalyticsService;
import com.gestione.ristoranti.gestione_ristoranti.common.RestResponse;
import com.gestione.ristoranti.gestione_ristoranti.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Dashboard e statistiche — solo ADMIN")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "KPI principali", description = "Revenue, ordini totali e coperti medi per il periodo selezionato")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "KPI calcolati"),
        @ApiResponse(responseCode = "400", description = "Parametri data non validi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/kpi")
    public ResponseEntity<RestResponse<KpiResponse>> getKpi(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate da,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate a) {
        return ResponseEntity.ok(RestResponse.ok(analyticsService.getKpi(da, a)));
    }

    @Operation(summary = "Revenue giornaliera")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Serie temporale revenue giornaliera"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/revenue/giornaliero")
    public ResponseEntity<RestResponse<List<RevenuePointResponse>>> revenueGiornaliera(
            @RequestParam(defaultValue = "7") int giorni) {
        return ResponseEntity.ok(RestResponse.ok(analyticsService.revenueGiornaliera(giorni)));
    }

    @Operation(summary = "Revenue settimanale")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Serie temporale revenue settimanale"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/revenue/settimanale")
    public ResponseEntity<RestResponse<List<RevenuePointResponse>>> revenueSettimanale(
            @RequestParam(defaultValue = "8") int settimane) {
        return ResponseEntity.ok(RestResponse.ok(analyticsService.revenueSettimanale(settimane)));
    }

    @Operation(summary = "Top piatti per vendite")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista piatti più venduti"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/top-piatti")
    public ResponseEntity<RestResponse<List<TopPiattoResponse>>> topPiatti(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(RestResponse.ok(analyticsService.topPiatti(limit)));
    }

    @Operation(summary = "Statistiche metodi di pagamento")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Distribuzione pagamenti per metodo"),
        @ApiResponse(responseCode = "400", description = "Parametri data non validi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/metodi-pagamento")
    public ResponseEntity<RestResponse<List<MetodoPagamentoStatResponse>>> metodiPagamento(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate da,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate a) {
        return ResponseEntity.ok(RestResponse.ok(analyticsService.metodiPagamento(da, a)));
    }

    @Operation(summary = "Food cost per piatto")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Food cost di ogni piatto del menu"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/food-cost")
    public ResponseEntity<RestResponse<List<FoodCostResponse>>> foodCostPerPiatto() {
        return ResponseEntity.ok(RestResponse.ok(analyticsService.foodCostPerPiatto()));
    }

    @Operation(summary = "Food cost medio di periodo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Food cost medio nel periodo selezionato"),
        @ApiResponse(responseCode = "400", description = "Parametri data non validi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/food-cost/periodo")
    public ResponseEntity<RestResponse<BigDecimal>> foodCostPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate da,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate a) {
        return ResponseEntity.ok(RestResponse.ok(analyticsService.foodCostMedioPeriodo(da, a)));
    }
}
