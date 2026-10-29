package com.criadorslides.controller;

import com.criadorslides.dto.GeneratePresentationRequest;
import com.criadorslides.dto.PresentationResponse;
import com.criadorslides.service.PdfExportService;
import com.criadorslides.service.SlideGeneratorService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/slides")
public class SlideController {

    private final SlideGeneratorService slideGeneratorService;
    private final PdfExportService pdfExportService;
    private final com.criadorslides.service.MercadoPagoService mercadoPagoService;

    public SlideController(
            SlideGeneratorService slideGeneratorService,
            PdfExportService pdfExportService,
            com.criadorslides.service.MercadoPagoService mercadoPagoService
    ) {
        this.slideGeneratorService = slideGeneratorService;
        this.pdfExportService = pdfExportService;
        this.mercadoPagoService = mercadoPagoService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "slides-api"));
    }

    @PostMapping("/generate")
    public ResponseEntity<PresentationResponse> generate(@RequestBody GeneratePresentationRequest request) {
        PresentationResponse response = slideGeneratorService.generatePresentation(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/export-pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestBody PresentationResponse presentation,
            @RequestParam(value = "paymentId", required = false) String paymentId
    ) throws IOException {
        boolean isPaid = mercadoPagoService.isPedidoAprovado(paymentId);
        byte[] pdfBytes = pdfExportService.exportPresentationToPdf(presentation, isPaid);

        String safeFileName = (presentation.getPresentationTitle() != null ? presentation.getPresentationTitle() : "apresentacao")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\-_]", "-")
                .replaceAll("-+", "-");
        if (safeFileName.length() > 45) {
            safeFileName = safeFileName.substring(0, 45);
        }
        if (safeFileName.isBlank()) {
            safeFileName = "slides";
        }
        if (!isPaid) {
            safeFileName = safeFileName + "-rascunho";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", safeFileName + ".pdf");
        headers.setContentLength(pdfBytes.length);
        headers.set("X-Presentation-Paid", String.valueOf(isPaid));

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/history")
    public ResponseEntity<List<PresentationResponse>> history() {
        return ResponseEntity.ok(slideGeneratorService.listRecentPresentations());
    }
}

