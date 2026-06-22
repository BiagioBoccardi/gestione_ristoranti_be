package com.gestione.ristoranti.gestione_ristoranti.conti.service;

import com.gestione.ristoranti.gestione_ristoranti.auth.model.Conto;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.MetodoPagamento;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Ordine;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.OrdineItem;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.StatoOrdine;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.StatoTavolo;
import com.gestione.ristoranti.gestione_ristoranti.auth.model.Tavolo;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.TavoloRepository;
import com.gestione.ristoranti.gestione_ristoranti.conti.dto.ContoItemResponse;
import com.gestione.ristoranti.gestione_ristoranti.conti.dto.ContoResponse;
import com.gestione.ristoranti.gestione_ristoranti.conti.dto.PagaContoRequest;
import com.gestione.ristoranti.gestione_ristoranti.conti.dto.SplitBillResponse;
import com.gestione.ristoranti.gestione_ristoranti.conti.repository.ContoRepository;
import com.gestione.ristoranti.gestione_ristoranti.exception.ConflictException;
import com.gestione.ristoranti.gestione_ristoranti.exception.ResourceNotFoundException;
import com.gestione.ristoranti.gestione_ristoranti.ordini.repository.OrdineRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContoService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ContoRepository contoRepository;
    private final OrdineRepository ordineRepository;
    private final TavoloRepository tavoloRepository;

    /**
     * Apre il conto per un ordine consegnato. Se il conto esiste già lo restituisce
     * senza crearne uno nuovo (idempotente).
     *
     * @param ordineId id dell'ordine
     * @return conto aperto o esistente
     * @throws ConflictException se l'ordine non è nello stato CONSEGNATO
     */
    @Transactional
    public ContoResponse apriConto(Long ordineId) {
        if (contoRepository.existsByOrdineId(ordineId)) {
            return mapToResponse(contoRepository.findByOrdineId(ordineId)
                    .orElseThrow(() -> new ResourceNotFoundException("Conto non trovato")));
        }

        Ordine ordine = ordineRepository.findById(ordineId)
                .orElseThrow(() -> new ResourceNotFoundException("Ordine non trovato"));

        if (ordine.getStato() != StatoOrdine.CONSEGNATO) {
            throw new ConflictException("Il conto può essere aperto solo per ordini consegnati");
        }

        BigDecimal totale = calcolaTotale(ordine);
        Conto conto = new Conto(ordine, totale, false, MetodoPagamento.CONTANTI);
        return mapToResponse(contoRepository.save(conto));
    }

    @Transactional(readOnly = true)
    public ContoResponse getConto(Long ordineId) {
        Conto conto = contoRepository.findByOrdineId(ordineId)
                .orElseThrow(() -> new ResourceNotFoundException("Conto non trovato per l'ordine"));
        return mapToResponse(conto);
    }

    @Transactional
    public ContoResponse pagaConto(Long contoId, PagaContoRequest request) {
        Conto conto = contoRepository.findById(contoId)
                .orElseThrow(() -> new ResourceNotFoundException("Conto non trovato"));

        if (conto.getPagato()) {
            throw new ConflictException("Il conto è già stato pagato");
        }

        conto.setPagato(true);
        conto.setMetodo(request.getMetodo());
        conto.setPagamentoIl(LocalDateTime.now());

        Tavolo tavolo = conto.getOrdine().getTavolo();
        if (tavolo.getStato() == StatoTavolo.IN_ATTESA_CONTO) {
            tavolo.setStato(StatoTavolo.DA_PULIRE);
            tavoloRepository.save(tavolo);
        }

        return mapToResponse(contoRepository.save(conto));
    }

    /**
     * Divide il totale del conto tra N persone, arrotondando a 2 decimali (HALF_UP).
     *
     * @param contoId  id del conto
     * @param nPersone numero di persone (minimo 1)
     * @return struttura con totale, numero persone e quota per persona
     * @throws IllegalArgumentException se nPersone &lt; 1
     */
    @Transactional(readOnly = true)
    public SplitBillResponse calcolaSplit(Long contoId, int nPersone) {
        if (nPersone < 1) {
            throw new IllegalArgumentException("Il numero di persone deve essere almeno 1");
        }
        Conto conto = contoRepository.findById(contoId)
                .orElseThrow(() -> new ResourceNotFoundException("Conto non trovato"));

        BigDecimal quota = conto.getTotale()
                .divide(BigDecimal.valueOf(nPersone), 2, RoundingMode.HALF_UP);

        return SplitBillResponse.builder()
                .totale(conto.getTotale())
                .nPersone(nPersone)
                .quotaPerPersona(quota)
                .build();
    }

    public byte[] exportPdf(Long contoId) throws IOException {
        Conto conto = contoRepository.findById(contoId)
                .orElseThrow(() -> new ResourceNotFoundException("Conto non trovato"));

        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float margin = 50;
                float y = 780;
                float lineH = 18;

                cs.beginText();
                cs.setFont(fontBold, 18);
                cs.newLineAtOffset(margin, y);
                cs.showText("CONTO — Tavolo " + conto.getOrdine().getTavolo().getNumero());
                cs.endText();
                y -= lineH * 2;

                cs.beginText();
                cs.setFont(fontNormal, 10);
                cs.newLineAtOffset(margin, y);
                cs.showText("Ordine #" + conto.getOrdine().getId() +
                        "   Data: " + (conto.getPagamentoIl() != null ? conto.getPagamentoIl().format(FMT) : "—"));
                cs.endText();
                y -= lineH * 2;

                cs.beginText();
                cs.setFont(fontBold, 11);
                cs.newLineAtOffset(margin, y);
                cs.showText(String.format("%-40s %8s %12s %12s", "Piatto", "Qtà", "Prezzo", "Subtotale"));
                cs.endText();
                y -= 2;

                cs.moveTo(margin, y);
                cs.lineTo(545, y);
                cs.stroke();
                y -= lineH;

                for (OrdineItem item : conto.getOrdine().getItems()) {
                    BigDecimal prezzo = prezzoEffettivo(item);
                    BigDecimal sub = prezzo.multiply(BigDecimal.valueOf(item.getQuantita()));
                    String row = String.format("%-40s %8d %12.2f %12.2f",
                            truncate(item.getPiatto().getNome(), 38),
                            item.getQuantita(),
                            prezzo,
                            sub);
                    cs.beginText();
                    cs.setFont(fontNormal, 10);
                    cs.newLineAtOffset(margin, y);
                    cs.showText(row);
                    cs.endText();
                    y -= lineH;
                }

                y -= 4;
                cs.moveTo(margin, y);
                cs.lineTo(545, y);
                cs.stroke();
                y -= lineH;

                cs.beginText();
                cs.setFont(fontBold, 12);
                cs.newLineAtOffset(margin, y);
                cs.showText(String.format("TOTALE: %.2f EUR", conto.getTotale()));
                cs.endText();
                y -= lineH * 1.5f;

                cs.beginText();
                cs.setFont(fontNormal, 10);
                cs.newLineAtOffset(margin, y);
                String statoStr = conto.getPagato()
                        ? "Pagato con " + conto.getMetodo().name()
                        : "Non ancora pagato";
                cs.showText("Stato: " + statoStr);
                cs.endText();
            }

            doc.save(out);
            return out.toByteArray();
        }
    }

    public byte[] exportExcel(Long contoId) throws IOException {
        Conto conto = contoRepository.findById(contoId)
                .orElseThrow(() -> new ResourceNotFoundException("Conto non trovato"));

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Conto");

            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle moneyStyle = wb.createCellStyle();
            moneyStyle.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));

            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("Conto — Tavolo " + conto.getOrdine().getTavolo().getNumero());

            Row infoRow = sheet.createRow(1);
            infoRow.createCell(0).setCellValue("Ordine #" + conto.getOrdine().getId());
            infoRow.createCell(2).setCellValue(conto.getPagato()
                    ? "Pagato con " + conto.getMetodo().name()
                    : "Non pagato");

            Row header = sheet.createRow(3);
            String[] cols = {"Piatto", "Quantità", "Prezzo Unitario (€)", "Subtotale (€)", "Note"};
            for (int i = 0; i < cols.length; i++) {
                var cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 4;
            for (OrdineItem item : conto.getOrdine().getItems()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(item.getPiatto().getNome());
                row.createCell(1).setCellValue(item.getQuantita());

                var priceCell = row.createCell(2);
                priceCell.setCellValue(prezzoEffettivo(item).doubleValue());
                priceCell.setCellStyle(moneyStyle);

                var subCell = row.createCell(3);
                subCell.setCellValue(prezzoEffettivo(item)
                        .multiply(BigDecimal.valueOf(item.getQuantita())).doubleValue());
                subCell.setCellStyle(moneyStyle);

                row.createCell(4).setCellValue(item.getNote() != null ? item.getNote() : "");
            }

            Row totaleRow = sheet.createRow(rowIdx + 1);
            Font boldFont = wb.createFont();
            boldFont.setBold(true);
            CellStyle boldStyle = wb.createCellStyle();
            boldStyle.setFont(boldFont);
            var totLabel = totaleRow.createCell(2);
            totLabel.setCellValue("TOTALE");
            totLabel.setCellStyle(boldStyle);
            var totCell = totaleRow.createCell(3);
            totCell.setCellValue(conto.getTotale().doubleValue());
            totCell.setCellStyle(moneyStyle);

            for (int i = 0; i < 5; i++) sheet.autoSizeColumn(i);

            wb.write(out);
            return out.toByteArray();
        }
    }

    private BigDecimal prezzoEffettivo(OrdineItem item) {
        return item.getPrezzoUnitario() != null
                ? item.getPrezzoUnitario()
                : item.getPiatto().getPrezzo();
    }

    private BigDecimal calcolaTotale(Ordine ordine) {
        return ordine.getItems().stream()
                .map(i -> prezzoEffettivo(i).multiply(BigDecimal.valueOf(i.getQuantita())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ContoResponse mapToResponse(Conto conto) {
        List<ContoItemResponse> items = conto.getOrdine().getItems().stream()
                .map(item -> {
                    BigDecimal sub = prezzoEffettivo(item)
                            .multiply(BigDecimal.valueOf(item.getQuantita()));
                    return ContoItemResponse.builder()
                            .id(item.getId())
                            .nomePiatto(item.getPiatto().getNome())
                            .quantita(item.getQuantita())
                            .prezzoUnitario(prezzoEffettivo(item))
                            .subtotale(sub)
                            .note(item.getNote())
                            .build();
                })
                .collect(Collectors.toList());

        return ContoResponse.builder()
                .id(conto.getId())
                .ordineId(conto.getOrdine().getId())
                .numeroTavolo(conto.getOrdine().getTavolo().getNumero())
                .items(items)
                .totale(conto.getTotale())
                .pagato(conto.getPagato())
                .metodo(conto.getMetodo())
                .pagamentoIl(conto.getPagamentoIl())
                .build();
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
