package com.gestione.ristoranti.gestione_ristoranti.qr.controller;

import com.gestione.ristoranti.gestione_ristoranti.common.RestResponse;
import com.gestione.ristoranti.gestione_ristoranti.exception.ErrorResponse;
import com.gestione.ristoranti.gestione_ristoranti.exception.ResourceNotFoundException;
import com.gestione.ristoranti.gestione_ristoranti.ordini.dto.OrdineRequest;
import com.gestione.ristoranti.gestione_ristoranti.ordini.dto.OrdineResponse;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.TavoloRepository;
import com.gestione.ristoranti.gestione_ristoranti.ordini.service.OrdineService;
import com.gestione.ristoranti.gestione_ristoranti.qr.dto.MenuQrResponse;
import com.gestione.ristoranti.gestione_ristoranti.qr.service.MenuQrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@SecurityRequirements
@Tag(name = "Menu QR (pubblico)", description = "Endpoint pubblici accessibili tramite QR code")
public class MenuQrController {

    private final MenuQrService menuQrService;
    private final OrdineService ordineService;
    private final TavoloRepository tavoloRepository;

    @Operation(summary = "Visualizza menu tramite QR", description = "Restituisce il menu del tavolo associato al token QR")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Menu trovato"),
        @ApiResponse(responseCode = "404", description = "Token QR non valido",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/menu/{token}")
    public ResponseEntity<RestResponse<MenuQrResponse>> getMenuByQr(@PathVariable String token) {
        return ResponseEntity.ok(RestResponse.ok(menuQrService.getMenuByToken(token)));
    }

    @Operation(summary = "Invia ordine tramite QR", description = "Crea un ordine anonimo dal tavolo identificato dal token QR")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Ordine creato"),
        @ApiResponse(responseCode = "400", description = "Dati non validi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Token QR non valido",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/ordini/qr/{token}")
    public ResponseEntity<RestResponse<OrdineResponse>> creaOrdineQr(
            @PathVariable String token,
            @Valid @RequestBody OrdineRequest request) {

        tavoloRepository.findByQrToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("QR code non valido"));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestResponse.created(ordineService.createAnonimo(request)));
    }
}
