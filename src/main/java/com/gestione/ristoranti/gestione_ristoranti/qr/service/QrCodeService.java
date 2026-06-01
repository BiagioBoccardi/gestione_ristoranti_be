package com.gestione.ristoranti.gestione_ristoranti.qr.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Tavolo;
import com.gestione.ristoranti.gestione_ristoranti.exception.ResourceNotFoundException;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.TavoloRepository;
import com.gestione.ristoranti.gestione_ristoranti.qr.dto.QrCodeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QrCodeService {

    private final TavoloRepository tavoloRepository;

    @Value("${app.base-url:http://localhost:3000}")
    private String baseUrl;

    @Transactional
    public byte[] generaQrCode(Long tavoloId) {
        Tavolo tavolo = tavoloRepository.findById(tavoloId)
                .orElseThrow(() -> new ResourceNotFoundException("Tavolo non trovato"));

        if (tavolo.getQrToken() == null) {
            tavolo.setQrToken(UUID.randomUUID().toString());
            tavoloRepository.save(tavolo);
        }

        return buildQrImage(tavolo.getQrToken());
    }

    @Transactional
    public byte[] rigeneraQrCode(Long tavoloId) {
        Tavolo tavolo = tavoloRepository.findById(tavoloId)
                .orElseThrow(() -> new ResourceNotFoundException("Tavolo non trovato"));

        tavolo.setQrToken(UUID.randomUUID().toString());
        tavoloRepository.save(tavolo);

        return buildQrImage(tavolo.getQrToken());
    }

    public QrCodeResponse getQrInfo(Long tavoloId) {
        Tavolo tavolo = tavoloRepository.findById(tavoloId)
                .orElseThrow(() -> new ResourceNotFoundException("Tavolo non trovato"));

        String token = tavolo.getQrToken();
        return QrCodeResponse.builder()
                .tavoloId(tavolo.getId())
                .numeroTavolo(tavolo.getNumero())
                .qrToken(token)
                .menuUrl(token != null ? baseUrl + "/menu/qr/" + token : null)
                .build();
    }

    private byte[] buildQrImage(String token) {
        String url = baseUrl + "/menu/qr/" + token;
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, 300, 300);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Errore nella generazione del QR code", e);
        }
    }
}