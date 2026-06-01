package com.gestione.ristoranti.gestione_ristoranti.qr.controller;

import com.gestione.ristoranti.gestione_ristoranti.common.RestResponse;
import com.gestione.ristoranti.gestione_ristoranti.exception.ErrorResponse;
import com.gestione.ristoranti.gestione_ristoranti.qr.dto.QrCodeResponse;
import com.gestione.ristoranti.gestione_ristoranti.qr.service.QrCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
@Tag(name = "QR Code", description = "Generazione e gestione QR code per tavoli — solo ADMIN")
public class QrController {

    private final QrCodeService qrCodeService;

    @Operation(summary = "Scarica immagine QR code", description = "Restituisce il QR code PNG del tavolo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Immagine PNG"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Tavolo non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(value = "/{tavoloId}", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> getQrCode(@PathVariable Long tavoloId) {
        byte[] image = qrCodeService.generaQrCode(tavoloId);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(image);
    }

    @Operation(summary = "Info QR code tavolo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Info QR trovate"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Tavolo non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{tavoloId}/info")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RestResponse<QrCodeResponse>> getQrInfo(@PathVariable Long tavoloId) {
        return ResponseEntity.ok(RestResponse.ok(qrCodeService.getQrInfo(tavoloId)));
    }

    @Operation(summary = "Rigenera QR code", description = "Genera un nuovo token QR per il tavolo, invalidando il precedente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Nuova immagine PNG"),
        @ApiResponse(responseCode = "403", description = "Non autorizzato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Tavolo non trovato",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{tavoloId}/rigenera")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> rigeneraQrCode(@PathVariable Long tavoloId) {
        byte[] image = qrCodeService.rigeneraQrCode(tavoloId);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(image);
    }
}
