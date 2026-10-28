package com.criadorslides;

import com.criadorslides.dto.*;
import com.criadorslides.service.PdfExportService;
import com.criadorslides.service.SlideGeneratorService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PdfExportTest {

    public static void main(String[] args) throws Exception {
        SlideGeneratorService generator = new SlideGeneratorService(null, new ObjectMapper());
        PdfExportService pdfService = new PdfExportService();

        List<String> allLayouts = Arrays.asList(
                "cover-circle",
                "cover-diagonal",
                "split-photo-left",
                "split-photo-right",
                "columns-4",
                "grid-4-cards",
                "infographic-stats",
                "timeline-steps",
                "quote-vision",
                "closing-thankyou"
        );

        GeneratePresentationRequest req = new GeneratePresentationRequest();
        req.setIdea("Teorema de Pitágoras e Aplicações na Geometria e Física");
        req.setSubjectTheme("matematica");
        req.setTemplateStyle("geometric-modern");
        req.setTotalPages(10);
        req.setAutoColors(true);

        List<SlidePageRequest> pageReqs = new ArrayList<>();
        for (int i = 0; i < allLayouts.size(); i++) {
            SlidePageRequest pr = new SlidePageRequest();
            pr.setPageNumber(i + 1);
            pr.setLayoutType(allLayouts.get(i));
            pr.setCustomTitle("Slide Página " + (i + 1));
            pageReqs.add(pr);
        }
        req.setPages(pageReqs);

        PresentationResponse resp = generator.generatePresentation(req);
        byte[] pdfBytes = pdfService.exportPresentationToPdf(resp);

        if (pdfBytes == null || pdfBytes.length < 50_000) {
            throw new IllegalStateException("PDF generation failed or too small: " + (pdfBytes != null ? pdfBytes.length : 0));
        }
        System.out.println("SUCCESS! Generated 10-page 16:9 PDF with " + pdfBytes.length + " bytes.");
    }
}
