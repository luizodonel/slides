package com.criadorslides.service;

import com.criadorslides.dto.PresentationResponse;
import com.criadorslides.dto.SlidePageDto;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class PdfExportService {

    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;

    // 16:9 PDF page size in points (960 x 540 pt)
    private static final float PDF_WIDTH = 960f;
    private static final float PDF_HEIGHT = 540f;

    public byte[] exportPresentationToPdf(PresentationResponse presentation) throws IOException {
        return exportPresentationToPdf(presentation, true);
    }

    public byte[] exportPresentationToPdf(PresentationResponse presentation, boolean isPaid) throws IOException {
        Color primary = parseColor(presentation.getPrimaryColor(), new Color(249, 115, 22));
        Color secondary = parseColor(presentation.getSecondaryColor(), new Color(30, 41, 59));
        String mainTitle = presentation.getPresentationTitle() != null ? presentation.getPresentationTitle() : "APRESENTAÇÃO";
        String subject = presentation.getSubjectTheme() != null ? presentation.getSubjectTheme().toUpperCase() : "SLIDES";

        List<SlidePageDto> slides = presentation.getSlides();
        int totalPages = (slides != null) ? slides.size() : 0;

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            if (slides != null) {
                for (int i = 0; i < slides.size(); i++) {
                    SlidePageDto slide = slides.get(i);
                    BufferedImage slideImg = renderSlideImage(slide, i + 1, totalPages, primary, secondary, mainTitle, subject, isPaid);

                    PDPage page = new PDPage(new PDRectangle(PDF_WIDTH, PDF_HEIGHT));
                    document.addPage(page);

                    PDImageXObject pdImage = LosslessFactory.createFromImage(document, slideImg);
                    try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                        contentStream.drawImage(pdImage, 0, 0, PDF_WIDTH, PDF_HEIGHT);
                    }
                }
            }

            document.save(out);
            return out.toByteArray();
        }
    }

    private BufferedImage renderSlideImage(
            SlidePageDto slide,
            int pageNum,
            int totalPages,
            Color primary,
            Color secondary,
            String mainTitle,
            String subject,
            boolean isPaid
    ) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        // Base crisp white background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        BufferedImage userPhoto = decodeBase64Photo(slide.getPhotoBase64());
        String layout = slide.getLayoutType() != null ? slide.getLayoutType() : "split-photo-left";

        switch (layout) {
            case "cover-circle" -> renderCoverCircle(g, slide, primary, secondary, userPhoto);
            case "cover-diagonal" -> renderCoverDiagonal(g, slide, primary, secondary, userPhoto);
            case "split-photo-left" -> renderSplitPhotoLeft(g, slide, primary, secondary, userPhoto);
            case "split-photo-right" -> renderSplitPhotoRight(g, slide, primary, secondary, userPhoto);
            case "columns-4" -> renderColumns4(g, slide, primary, secondary, userPhoto);
            case "grid-4-cards" -> renderGrid4Cards(g, slide, primary, secondary, userPhoto);
            case "infographic-stats" -> renderInfographicStats(g, slide, primary, secondary, userPhoto);
            case "timeline-steps" -> renderTimelineSteps(g, slide, primary, secondary, userPhoto);
            case "quote-vision" -> renderQuoteVision(g, slide, primary, secondary, userPhoto);
            case "closing-thankyou" -> renderClosingThankYou(g, slide, primary, secondary, userPhoto);
            default -> renderSplitPhotoLeft(g, slide, primary, secondary, userPhoto);
        }

        // Subtle Footer bar & Page Number badge
        drawSlideFooter(g, pageNum, totalPages, mainTitle, primary, secondary, layout);

        if (!isPaid) {
            drawDraftWatermark(g);
        }

        g.dispose();
        return img;
    }

    private void drawDraftWatermark(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.rotate(Math.toRadians(-28), WIDTH / 2.0, HEIGHT / 2.0);

        // Faixa diagonal de fundo translúcida
        g2.setColor(new Color(220, 38, 38, 55));
        g2.fillRect(-WIDTH, HEIGHT / 2 - 130, WIDTH * 3, 260);

        // Bordas da faixa
        g2.setColor(new Color(220, 38, 38, 140));
        g2.setStroke(new BasicStroke(6f));
        g2.drawRect(-WIDTH, HEIGHT / 2 - 130, WIDTH * 3, 260);

        // Texto principal
        g2.setColor(new Color(185, 28, 28, 210));
        g2.setFont(new Font("SansSerif", Font.BOLD, 120));
        String text1 = "RASCUNHO";
        FontMetrics fm1 = g2.getFontMetrics();
        int textX1 = (WIDTH - fm1.stringWidth(text1)) / 2;
        g2.drawString(text1, textX1, HEIGHT / 2 - 10);

        // Subtexto
        g2.setFont(new Font("SansSerif", Font.BOLD, 40));
        String text2 = "SLIDES DA ONINHA — PRÉ-VISUALIZAÇÃO";
        FontMetrics fm2 = g2.getFontMetrics();
        int textX2 = (WIDTH - fm2.stringWidth(text2)) / 2;
        g2.drawString(text2, textX2, HEIGHT / 2 + 65);

        g2.dispose();
    }

    // 1. COVER CIRCLE (Inspired by Image 1 top-left & Image 5 geometric cover)
    private void renderCoverCircle(Graphics2D g, SlidePageDto slide, Color primary, Color secondary, BufferedImage photo) {
        // Left 62% primary color block
        g.setColor(primary);
        g.fillRect(0, 0, 1180, HEIGHT);

        // Bottom left 3-tone geometric rectangles (inspired by Image 5)
        g.setColor(secondary);
        g.fillRect(0, HEIGHT - 150, 380, 150);
        g.setColor(blend(primary, Color.BLACK, 0.22f));
        g.fillRect(380, HEIGHT - 150, 380, 150);
        g.setColor(blend(primary, Color.WHITE, 0.25f));
        g.fillRect(760, HEIGHT - 150, 420, 150);

        // Top right decorative geometric bars
        g.setColor(secondary);
        g.fillRect(1420, 0, 500, 90);
        g.setColor(blend(primary, Color.WHITE, 0.35f));
        g.fillRect(1620, 90, 300, 28);

        // Circular Photo Frame overlapping boundary at (1230, 510) radius 360
        int cx = 1260, cy = 510, r = 350;
        g.setColor(Color.WHITE);
        g.fillOval(cx - r - 24, cy - r - 24, (r + 24) * 2, (r + 24) * 2);

        g.setColor(secondary);
        g.fillOval(cx - r - 8, cy - r - 8, (r + 8) * 2, (r + 8) * 2);

        Shape oldClip = g.getClip();
        Ellipse2D circleClip = new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2);
        g.setClip(circleClip);
        drawPhotoOrPlaceholder(g, photo, cx - r, cy - r, r * 2, r * 2, primary, secondary, slide.getPhotoCaption());
        g.setClip(oldClip);

        // Main Cover Title
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 66));
        int nextY = drawWrappedText(g, safe(slide.getTitle(), "TÍTULO DA APRESENTAÇÃO"), 100, 310, 740, 78, 4);

        // Accent divider line
        g.setColor(secondary);
        g.fillRect(100, nextY + 10, 140, 10);

        // Subtitle
        g.setColor(new Color(255, 255, 255, 235));
        g.setFont(new Font("SansSerif", Font.PLAIN, 28));
        nextY = drawWrappedText(g, safe(slide.getSubtitle(), ""), 100, nextY + 65, 720, 40, 3);

        // Bullets pills at bottom
        List<String> bullets = slide.getBullets();
        if (bullets != null && !bullets.isEmpty()) {
            int by = Math.min(nextY + 45, 760);
            g.setFont(new Font("SansSerif", Font.BOLD, 21));
            for (int i = 0; i < Math.min(3, bullets.size()); i++) {
                g.setColor(new Color(0, 0, 0, 45));
                g.fillRoundRect(100, by - 28, 680, 42, 12, 12);
                g.setColor(Color.WHITE);
                g.drawString("▸  " + bullets.get(i), 120, by);
                by += 52;
            }
        }
    }

    // 2. COVER DIAGONAL (Inspired by Image 3 top-left diagonal split + diamond/chevron)
    private void renderCoverDiagonal(Graphics2D g, SlidePageDto slide, Color primary, Color secondary, BufferedImage photo) {
        // Right side photo or dark geometric area (shifted right so stripes never cover the text)
        Polygon rightPoly = new Polygon(new int[]{980, WIDTH, WIDTH, 1360}, new int[]{0, 0, HEIGHT, HEIGHT}, 4);
        Shape oldClip = g.getClip();
        g.setClip(rightPoly);
        drawPhotoOrPlaceholder(g, photo, 980, 0, WIDTH - 980, HEIGHT, primary, secondary, slide.getPhotoCaption());
        // Dark overlay for contrast
        g.setColor(new Color(secondary.getRed(), secondary.getGreen(), secondary.getBlue(), 95));
        g.fillRect(980, 0, WIDTH - 980, HEIGHT);
        g.setClip(oldClip);

        // Diagonal thick primary stripe
        Polygon stripe1 = new Polygon(new int[]{920, 1010, 1410, 1320}, new int[]{0, 0, HEIGHT, HEIGHT}, 4);
        g.setColor(primary);
        g.fillPolygon(stripe1);

        // Diagonal secondary stripe
        Polygon stripe2 = new Polygon(new int[]{870, 920, 1320, 1270}, new int[]{0, 0, HEIGHT, HEIGHT}, 4);
        g.setColor(secondary);
        g.fillPolygon(stripe2);

        // Left side content (x = 100 .. 820, always left of the stripes)
        g.setColor(secondary);
        g.setFont(new Font("SansSerif", Font.BOLD, 60));
        int y = drawWrappedText(g, safe(slide.getTitle(), "TÍTULO DO SLIDE"), 100, 265, 720, 72, 4);

        g.setColor(primary);
        g.fillRect(100, y + 15, 180, 10);

        g.setColor(new Color(71, 85, 105));
        g.setFont(new Font("SansSerif", Font.PLAIN, 28));
        y = drawWrappedText(g, safe(slide.getSubtitle(), ""), 100, y + 70, 700, 40, 3);

        // Bullets
        List<String> bullets = slide.getBullets();
        if (bullets != null) {
            int by = Math.max(y + 40, 680);
            for (int i = 0; i < Math.min(3, bullets.size()); i++) {
                g.setColor(primary);
                g.fillOval(100, by - 22, 24, 24);
                g.setColor(secondary);
                g.setFont(new Font("SansSerif", Font.BOLD, 23));
                g.drawString(truncate(bullets.get(i), 50), 140, by - 2);
                by += 52;
            }
        }
    }

    // 3. SPLIT PHOTO LEFT (Inspired by Image 1 top-center & Image 5 second slide)
    private void renderSplitPhotoLeft(Graphics2D g, SlidePageDto slide, Color primary, Color secondary, BufferedImage photo) {
        // Left vertical primary pillar
        g.setColor(primary);
        g.fillRect(140, 0, 440, HEIGHT);

        // Secondary top & bottom accent blocks
        g.setColor(secondary);
        g.fillRect(0, 0, 140, 220);
        g.fillRect(1140, HEIGHT - 130, 780, 130);
        g.setColor(blend(primary, Color.WHITE, 0.35f));
        g.fillRect(920, HEIGHT - 130, 220, 130);

        // Framed Photo overlapping the left pillar
        int px = 95, py = 150, pw = 680, ph = 720;
        g.setColor(Color.WHITE);
        g.fillRect(px - 14, py - 14, pw + 28, ph + 28);
        g.setColor(secondary);
        g.fillRect(px, py, pw, ph);
        drawPhotoOrPlaceholder(g, photo, px, py, pw, ph, primary, secondary, slide.getPhotoCaption());

        // Right side content (x = 860 .. 1820)
        int rx = 860;
        g.setColor(secondary);
        g.setFont(new Font("SansSerif", Font.BOLD, 48));
        int y = drawWrappedText(g, safe(slide.getTitle(), "TÍTULO DA SEÇÃO"), rx, 220, 960, 58, 3);

        g.setColor(primary);
        g.fillRect(rx, y + 8, 120, 8);

        g.setColor(new Color(51, 65, 85));
        g.setFont(new Font("SansSerif", Font.BOLD, 26));
        y = drawWrappedText(g, safe(slide.getSubtitle(), ""), rx, y + 58, 940, 36, 2);

        g.setColor(new Color(71, 85, 105));
        g.setFont(new Font("SansSerif", Font.PLAIN, 24));
        y = drawWrappedText(g, safe(slide.getBodyText(), ""), rx, y + 25, 940, 36, 5);

        // Bullets with numbered badges
        List<String> bullets = slide.getBullets();
        if (bullets != null && !bullets.isEmpty()) {
            int by = Math.max(y + 30, 620);
            for (int i = 0; i < Math.min(3, bullets.size()); i++) {
                g.setColor(primary);
                g.fillRoundRect(rx, by - 32, 48, 42, 10, 10);
                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, 20));
                g.drawString("0" + (i + 1), rx + 12, by - 4);

                g.setColor(secondary);
                g.setFont(new Font("SansSerif", Font.PLAIN, 23));
                drawWrappedText(g, bullets.get(i), rx + 68, by - 4, 870, 30, 2);
                by += 78;
            }
        }
    }

    // 4. SPLIT PHOTO RIGHT (Inspired by Image 2 right-accent & Image 4 split)
    private void renderSplitPhotoRight(Graphics2D g, SlidePageDto slide, Color primary, Color secondary, BufferedImage photo) {
        // Right primary background block
        g.setColor(primary);
        g.fillRect(1280, 0, WIDTH - 1280, HEIGHT);

        // Secondary geometric block
        g.setColor(secondary);
        g.fillRect(1140, 740, 780, 220);

        // Photo Frame on the right
        int px = 1080, py = 130, pw = 720, ph = 680;
        g.setColor(Color.WHITE);
        g.fillRect(px - 14, py - 14, pw + 28, ph + 28);
        drawPhotoOrPlaceholder(g, photo, px, py, pw, ph, primary, secondary, slide.getPhotoCaption());

        // Left side content (x = 100 .. 990)
        int lx = 100;
        g.setColor(secondary);
        g.setFont(new Font("SansSerif", Font.BOLD, 48));
        int y = drawWrappedText(g, safe(slide.getTitle(), "DESENVOLVIMENTO DO TEMA"), lx, 220, 900, 58, 3);

        g.setColor(primary);
        g.fillRect(lx, y + 8, 140, 8);

        g.setColor(new Color(71, 85, 105));
        g.setFont(new Font("SansSerif", Font.PLAIN, 25));
        y = drawWrappedText(g, safe(slide.getBodyText(), ""), lx, y + 60, 900, 38, 5);

        // Highlight cards on left bottom
        List<String> bullets = slide.getBullets();
        if (bullets != null && !bullets.isEmpty()) {
            int by = Math.max(y + 35, 590);
            for (int i = 0; i < Math.min(3, bullets.size()); i++) {
                g.setColor(new Color(248, 250, 252));
                g.fillRoundRect(lx, by, 900, 86, 14, 14);
                g.setColor(i % 2 == 0 ? primary : secondary);
                g.fillRoundRect(lx, by, 12, 86, 6, 6);

                g.setColor(secondary);
                g.setFont(new Font("SansSerif", Font.BOLD, 22));
                drawWrappedText(g, bullets.get(i), lx + 34, by + 36, 840, 28, 2);
                by += 104;
            }
        }
    }

    // 5. COLUMNS 4 (Inspired by Image 3 01-02-03-04 columns & Image 1 middle center)
    private void renderColumns4(Graphics2D g, SlidePageDto slide, Color primary, Color secondary, BufferedImage photo) {
        // Top header band
        g.setColor(secondary);
        g.fillRect(0, 0, WIDTH, 250);
        g.setColor(primary);
        g.fillRect(0, 250, WIDTH, 14);

        // If user uploaded a photo on this page, render it as a circular/rounded badge in the top-right header
        if (photo != null) {
            g.setColor(Color.WHITE);
            g.fillRoundRect(WIDTH - 360, 28, 260, 194, 18, 18);
            drawPhotoOrPlaceholder(g, photo, WIDTH - 354, 34, 248, 182, primary, secondary, null);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 50));
        drawWrappedText(g, safe(slide.getTitle(), "OS 4 PILARES PRINCIPAIS"), 100, 140, photo != null ? 1380 : 1680, 58, 2);

        g.setColor(new Color(203, 213, 225));
        g.setFont(new Font("SansSerif", Font.PLAIN, 24));
        g.drawString(truncate(safe(slide.getSubtitle(), ""), 100), 100, 215);

        List<SlidePageDto.ColumnItemDto> cols = slide.getColumns();
        int colWidth = 395;
        int gap = 46;
        int startX = 100;
        int cardY = 320;
        int cardH = 620;

        for (int i = 0; i < 4; i++) {
            int cx = startX + i * (colWidth + gap);
            boolean isEven = (i % 2 == 0);
            Color accent = isEven ? primary : secondary;

            // Card shadow & body
            g.setColor(new Color(241, 245, 249));
            g.fillRoundRect(cx, cardY, colWidth, cardH, 20, 20);

            // Top colored header inside card
            g.setColor(accent);
            g.fillRoundRect(cx, cardY, colWidth, 150, 20, 20);
            g.fillRect(cx, cardY + 120, colWidth, 30);

            String num = (cols != null && i < cols.size() && cols.get(i).getNumber() != null)
                    ? cols.get(i).getNumber() : ("0" + (i + 1));
            String colTitle = (cols != null && i < cols.size() && cols.get(i).getTitle() != null)
                    ? cols.get(i).getTitle() : ("PILAR 0" + (i + 1));
            String colText = (cols != null && i < cols.size() && cols.get(i).getDescription() != null)
                    ? cols.get(i).getDescription() : "";

            // Big number 01, 02, 03, 04
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 68));
            g.drawString(num, cx + 32, cardY + 102);

            // Column Title
            g.setColor(secondary);
            g.setFont(new Font("SansSerif", Font.BOLD, 26));
            int ty = drawWrappedText(g, colTitle, cx + 32, cardY + 215, colWidth - 64, 34, 2);

            // Divider line
            g.setColor(primary);
            g.fillRect(cx + 32, ty + 8, 70, 6);

            // Column Description
            g.setColor(new Color(71, 85, 105));
            g.setFont(new Font("SansSerif", Font.PLAIN, 23));
            drawWrappedText(g, colText, cx + 32, ty + 55, colWidth - 64, 34, 7);

            // Bottom bar
            g.setColor(accent);
            g.fillRoundRect(cx, cardY + cardH - 18, colWidth, 18, 8, 8);
        }
    }

    // 6. GRID 4 CARDS (Inspired by Image 1 middle-right & Image 5 bottom-left 6/4 icon grid)
    private void renderGrid4Cards(Graphics2D g, SlidePageDto slide, Color primary, Color secondary, BufferedImage photo) {
        // Top banner with primary/secondary split
        g.setColor(primary);
        g.fillRect(0, 0, WIDTH, 260);
        g.setColor(secondary);
        Polygon rightHeaderPoly = new Polygon(new int[]{1300, WIDTH, WIDTH, 1180}, new int[]{0, 0, 260, 260}, 4);
        g.fillPolygon(rightHeaderPoly);

        if (photo != null) {
            Shape old = g.getClip();
            g.setClip(rightHeaderPoly);
            drawPhotoOrPlaceholder(g, photo, 1180, 0, WIDTH - 1180, 260, primary, secondary, null);
            g.setClip(old);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 52));
        drawWrappedText(g, safe(slide.getTitle(), "CONCEITOS E APLICAÇÕES"), 100, 145, 1050, 60, 2);

        g.setFont(new Font("SansSerif", Font.PLAIN, 24));
        g.drawString(truncate(safe(slide.getSubtitle(), ""), 85), 100, 225);

        List<SlidePageDto.ColumnItemDto> cards = slide.getColumns();
        int cardW = 820;
        int cardH = 300;
        int startX = 100;
        int startY = 320;
        int gapX = 80;
        int gapY = 50;

        for (int i = 0; i < 4; i++) {
            int row = i / 2;
            int col = i % 2;
            int x = startX + col * (cardW + gapX);
            int y = startY + row * (cardH + gapY);

            g.setColor(new Color(248, 250, 252));
            g.fillRoundRect(x, y, cardW, cardH, 20, 20);
            g.setColor(new Color(226, 232, 240));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(x, y, cardW, cardH, 20, 20);

            // Square icon badge on left of card (matches Image 1 middle-right)
            Color badgeColor = (i == 0 || i == 3) ? primary : secondary;
            g.setColor(badgeColor);
            g.fillRoundRect(x + 34, y + 40, 100, 100, 18, 18);

            String badgeLabel = (cards != null && i < cards.size() && cards.get(i).getNumber() != null)
                    ? cards.get(i).getNumber() : ("0" + (i + 1));
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 40));
            FontMetrics fm = g.getFontMetrics();
            int tx = x + 34 + (100 - fm.stringWidth(badgeLabel)) / 2;
            g.drawString(badgeLabel, tx, y + 104);

            String cTitle = (cards != null && i < cards.size()) ? safe(cards.get(i).getTitle(), "Tópico") : "Tópico";
            String cDesc = (cards != null && i < cards.size()) ? safe(cards.get(i).getDescription(), "") : "";

            g.setColor(secondary);
            g.setFont(new Font("SansSerif", Font.BOLD, 30));
            drawWrappedText(g, cTitle, x + 164, y + 72, cardW - 195, 36, 1);

            g.setColor(primary);
            g.fillRect(x + 164, y + 92, 85, 5);

            g.setColor(new Color(71, 85, 105));
            g.setFont(new Font("SansSerif", Font.PLAIN, 23));
            drawWrappedText(g, cDesc, x + 164, y + 138, cardW - 195, 34, 4);
        }
    }

    // 7. INFOGRAPHIC STATS / 3 PILARES EM DESTAQUE COM CÍRCULOS (Inspirado no modelo genérico da Ref.)
    private void renderInfographicStats(Graphics2D g, SlidePageDto slide, Color primary, Color secondary, BufferedImage photo) {
        // Left dark/secondary panel
        g.setColor(secondary);
        g.fillRect(0, 0, 860, HEIGHT);

        // Bottom 3-tone bar on left panel
        g.setColor(primary);
        g.fillRect(0, HEIGHT - 120, 290, 120);
        g.setColor(blend(primary, Color.WHITE, 0.25f));
        g.fillRect(290, HEIGHT - 120, 290, 120);
        g.setColor(blend(secondary, Color.WHITE, 0.15f));
        g.fillRect(580, HEIGHT - 120, 280, 120);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 52));
        int ly = drawWrappedText(g, safe(slide.getTitle(), "PILARES & DIRETRIZES"), 80, 210, 700, 62, 3);

        g.setColor(primary);
        g.fillRect(80, ly + 15, 80, 6);

        if (slide.getSubtitle() != null && !slide.getSubtitle().isBlank()) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 26));
            ly = drawWrappedText(g, slide.getSubtitle(), 80, ly + 55, 700, 36, 2);
        }

        g.setColor(new Color(203, 213, 225));
        g.setFont(new Font("SansSerif", Font.PLAIN, 23));
        ly = drawWrappedText(g, safe(slide.getBodyText(), ""), 80, ly + 40, 700, 34, 4);

        if (photo != null) {
            // Show user's uploaded photo inside left panel
            g.setColor(Color.WHITE);
            g.fillRoundRect(80, ly + 25, 700, Math.min(380, HEIGHT - ly - 160), 16, 16);
            drawPhotoOrPlaceholder(g, photo, 86, ly + 31, 688, Math.min(368, HEIGHT - ly - 172), primary, secondary, slide.getPhotoCaption());
        }

        // Right side 3 clean pillar cards with circles (matches web and Image 2)
        List<SlidePageDto.StatItemDto> stats = slide.getStats();
        List<SlidePageDto.ColumnItemDto> cols = slide.getColumns();

        int sy = 160;
        int cardW = 890;
        int cardH = 210;
        int gapY = 45;

        for (int i = 0; i < 3; i++) {
            String val = "0" + (i + 1);
            String lbl = "Pilar " + (i + 1);
            String desc = "";

            if (stats != null && i < stats.size()) {
                if (stats.get(i).getValue() != null && !stats.get(i).getValue().isBlank()) val = stats.get(i).getValue();
                if (stats.get(i).getLabel() != null && !stats.get(i).getLabel().isBlank()) lbl = stats.get(i).getLabel();
                if (stats.get(i).getDescription() != null) desc = stats.get(i).getDescription();
            } else if (cols != null && i < cols.size()) {
                if (cols.get(i).getNumber() != null && !cols.get(i).getNumber().isBlank()) val = cols.get(i).getNumber();
                if (cols.get(i).getTitle() != null && !cols.get(i).getTitle().isBlank()) lbl = cols.get(i).getTitle();
                if (cols.get(i).getDescription() != null) desc = cols.get(i).getDescription();
            }

            int cy = sy + i * (cardH + gapY);

            // Card background & border
            g.setColor(new Color(248, 250, 252));
            g.fillRoundRect(950, cy, cardW, cardH, 20, 20);
            g.setColor(new Color(226, 232, 240));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(950, cy, cardW, cardH, 20, 20);

            // Circular badge
            Color circleColor = (i % 2 == 0) ? primary : secondary;
            g.setColor(circleColor);
            g.fillOval(985, cy + 50, 110, 110);

            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, val.length() > 2 ? 30 : 44));
            FontMetrics fm = g.getFontMetrics();
            int tx = 985 + (110 - fm.stringWidth(val)) / 2;
            g.drawString(val, tx, cy + 120);

            // Title & Description
            g.setColor(secondary);
            g.setFont(new Font("SansSerif", Font.BOLD, 30));
            drawWrappedText(g, lbl.toUpperCase(), 1130, cy + 65, cardW - 200, 36, 1);

            g.setColor(new Color(71, 85, 105));
            g.setFont(new Font("SansSerif", Font.PLAIN, 23));
            drawWrappedText(g, desc, 1130, cy + 115, cardW - 200, 32, 3);
        }
    }

    // 8. TIMELINE STEPS (Inspired by Image 4 top-right 4 chevrons/steps & Image 2 process)
    private void renderTimelineSteps(Graphics2D g, SlidePageDto slide, Color primary, Color secondary, BufferedImage photo) {
        g.setColor(primary);
        g.fillRect(0, 0, 18, HEIGHT);

        g.setColor(secondary);
        g.setFont(new Font("SansSerif", Font.BOLD, 50));
        drawWrappedText(g, safe(slide.getTitle(), "ETAPAS DO PROCESSO"), 100, 175, photo != null ? 1200 : 1680, 58, 2);

        g.setColor(new Color(100, 116, 139));
        g.setFont(new Font("SansSerif", Font.PLAIN, 25));
        g.drawString(truncate(safe(slide.getSubtitle(), ""), 95), 100, 250);

        if (photo != null) {
            g.setColor(secondary);
            g.fillRoundRect(1420, 60, 400, 220, 16, 16);
            drawPhotoOrPlaceholder(g, photo, 1426, 66, 388, 208, primary, secondary, null);
        }

        // Horizontal connecting line
        int lineY = 440;
        g.setColor(new Color(226, 232, 240));
        g.fillRect(180, lineY - 6, 1560, 12);

        List<SlidePageDto.ColumnItemDto> steps = slide.getColumns();
        int stepW = 390;
        int gap = 53;
        int startX = 100;

        for (int i = 0; i < 4; i++) {
            int x = startX + i * (stepW + gap);
            Color c = (i % 2 == 0) ? primary : secondary;

            // Chevron / Circle Node
            int cx = x + stepW / 2;
            g.setColor(Color.WHITE);
            g.fillOval(cx - 64, lineY - 64, 128, 128);
            g.setColor(c);
            g.fillOval(cx - 52, lineY - 52, 104, 104);

            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 34));
            g.drawString("0" + (i + 1), cx - 22, lineY + 13);

            // Step Card below node
            int cardY = 535;
            int cardH = 380;
            g.setColor(new Color(248, 250, 252));
            g.fillRoundRect(x, cardY, stepW, cardH, 18, 18);
            g.setColor(c);
            g.fillRoundRect(x, cardY, stepW, 14, 8, 8);

            String sTitle = (steps != null && i < steps.size()) ? safe(steps.get(i).getTitle(), "Etapa " + (i + 1)) : ("Etapa " + (i + 1));
            String sDesc = (steps != null && i < steps.size()) ? safe(steps.get(i).getDescription(), "") : "";

            g.setColor(secondary);
            g.setFont(new Font("SansSerif", Font.BOLD, 27));
            int ty = drawWrappedText(g, sTitle, x + 28, cardY + 68, stepW - 56, 34, 2);

            g.setColor(new Color(71, 85, 105));
            g.setFont(new Font("SansSerif", Font.PLAIN, 22));
            drawWrappedText(g, sDesc, x + 28, ty + 28, stepW - 56, 33, 6);
        }
    }

    // 9. QUOTE & VISION/MISSION (Inspired by Image 5 top-right Vision/Mission & middle-right Quote block)
    private void renderQuoteVision(Graphics2D g, SlidePageDto slide, Color primary, Color secondary, BufferedImage photo) {
        // Left background subtle gray or user photo
        if (photo != null) {
            drawPhotoOrPlaceholder(g, photo, 0, 0, 820, HEIGHT, primary, secondary, slide.getPhotoCaption());
            g.setColor(new Color(secondary.getRed(), secondary.getGreen(), secondary.getBlue(), 140));
            g.fillRect(0, 0, 820, HEIGHT);
        } else {
            g.setColor(new Color(241, 245, 249));
            g.fillRect(0, 0, 820, HEIGHT);
        }

        // Overlapping Primary Quote Block (x = 120 .. 880, y = 160 .. 880)
        g.setColor(primary);
        g.fillRoundRect(110, 160, 760, 720, 20, 20);

        // Big quotation mark
        g.setColor(secondary);
        g.setFont(new Font("Serif", Font.BOLD, 170));
        g.drawString("“", 165, 330);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 34));
        String quote = safe(slide.getHighlightQuote(), "“A clareza conceitual e o método estruturado transformam o aprendizado em resultados duradouros.”");
        drawWrappedText(g, quote, 175, 410, 630, 48, 8);

        // Right side Title + Vision / Mission columns (x = 940 .. 1820)
        g.setColor(secondary);
        g.setFont(new Font("SansSerif", Font.BOLD, 48));
        int ry = drawWrappedText(g, safe(slide.getTitle(), "PRINCÍPIO CENTRAL"), 950, 225, 860, 58, 2);

        List<SlidePageDto.ColumnItemDto> cols = slide.getColumns();
        int boxY = Math.max(ry + 40, 350);
        int boxW = 415;
        int boxH = 530;

        for (int i = 0; i < 2; i++) {
            int bx = 950 + i * (boxW + 35);
            Color bg = (i == 0) ? secondary : new Color(248, 250, 252);
            Color fgTitle = (i == 0) ? primary : secondary;
            Color fgText = (i == 0) ? new Color(226, 232, 240) : new Color(71, 85, 105);

            g.setColor(bg);
            g.fillRoundRect(bx, boxY, boxW, boxH, 18, 18);
            g.setColor(primary);
            g.fillRect(bx, boxY, boxW, 12);

            String cTitle = (cols != null && i < cols.size()) ? safe(cols.get(i).getTitle(), i == 0 ? "VISÃO" : "MISSÃO") : (i == 0 ? "VISÃO" : "MISSÃO");
            String cDesc = (cols != null && i < cols.size()) ? safe(cols.get(i).getDescription(), "") : "";

            g.setColor(fgTitle);
            g.setFont(new Font("SansSerif", Font.BOLD, 30));
            int ty = drawWrappedText(g, cTitle, bx + 36, boxY + 85, boxW - 72, 36, 2);

            g.setColor(primary);
            g.fillRect(bx + 36, ty + 10, 65, 5);

            g.setColor(fgText);
            g.setFont(new Font("SansSerif", Font.PLAIN, 24));
            drawWrappedText(g, cDesc, bx + 36, ty + 62, boxW - 72, 36, 8);
        }
    }

    // 10. CLOSING THANK YOU (Inspired by Image 5 bottom-right dark + 3-tone blocks + photo frame)
    private void renderClosingThankYou(Graphics2D g, SlidePageDto slide, Color primary, Color secondary, BufferedImage photo) {
        // Full dark/secondary background
        g.setColor(secondary);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Bottom left 3-tone geometric bars
        g.setColor(primary);
        g.fillRect(0, HEIGHT - 170, 380, 170);
        g.setColor(blend(primary, Color.BLACK, 0.25f));
        g.fillRect(380, HEIGHT - 170, 380, 170);
        g.setColor(blend(primary, Color.WHITE, 0.3f));
        g.fillRect(760, HEIGHT - 170, 380, 170);

        // Right framed photo or geometric card
        int px = 1220, py = 140, pw = 560, ph = 640;
        g.setColor(primary);
        g.fillRect(px, py - 50, pw / 2, 50);
        g.setColor(blend(primary, Color.WHITE, 0.3f));
        g.fillRect(px + pw / 2, py - 50, pw / 2, 50);

        g.setColor(Color.WHITE);
        g.fillRect(px - 10, py - 10, pw + 20, ph + 20);
        drawPhotoOrPlaceholder(g, photo, px, py, pw, ph, primary, secondary, slide.getPhotoCaption());

        // Left side Thank You typography
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 68));
        int y = drawWrappedText(g, safe(slide.getTitle(), "OBRIGADO PELA ATENÇÃO!"), 120, 300, 980, 80, 2);

        g.setColor(primary);
        g.fillRect(120, y + 15, 220, 10);

        g.setColor(new Color(203, 213, 225));
        g.setFont(new Font("SansSerif", Font.PLAIN, 28));
        y = drawWrappedText(g, safe(slide.getSubtitle(), ""), 120, y + 75, 960, 40, 2);

        g.setColor(new Color(148, 163, 184));
        g.setFont(new Font("SansSerif", Font.PLAIN, 24));
        y = drawWrappedText(g, safe(slide.getBodyText(), ""), 120, y + 30, 960, 36, 3);

        List<String> bullets = slide.getBullets();
        if (bullets != null) {
            int by = Math.min(y + 45, 720);
            for (int i = 0; i < Math.min(3, bullets.size()); i++) {
                g.setColor(primary);
                g.fillOval(120, by - 20, 18, 18);
                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, 22));
                g.drawString(bullets.get(i), 155, by - 3);
                by += 46;
            }
        }
    }

    private void drawPhotoOrPlaceholder(
            Graphics2D g,
            BufferedImage photo,
            int x,
            int y,
            int w,
            int h,
            Color primary,
            Color secondary,
            String caption
    ) {
        Shape prevClip = g.getClip();
        g.clipRect(x, y, w, h);

        if (photo != null) {
            // Object-cover crop scaling
            double imgRatio = (double) photo.getWidth() / photo.getHeight();
            double boxRatio = (double) w / h;
            int drawW, drawH, drawX, drawY;
            if (imgRatio > boxRatio) {
                drawH = h;
                drawW = (int) (h * imgRatio);
                drawX = x - (drawW - w) / 2;
                drawY = y;
            } else {
                drawW = w;
                drawH = (int) (w / imgRatio);
                drawX = x;
                drawY = y - (drawH - h) / 2;
            }
            g.drawImage(photo, drawX, drawY, drawW, drawH, null);
        } else {
            // Sleek geometric vector illustration placeholder when user hasn't uploaded a photo for this slide
            GradientPaint gp = new GradientPaint(
                    x, y, blend(secondary, Color.BLACK, 0.1f),
                    x + w, y + h, blend(secondary, primary, 0.35f)
            );
            g.setPaint(gp);
            g.fillRect(x, y, w, h);

            // Geometric decorative circles & grid lines
            g.setColor(new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), 55));
            g.fillOval(x + w / 2 - 180, y + h / 2 - 180, 360, 360);
            g.setColor(new Color(255, 255, 255, 25));
            g.setStroke(new BasicStroke(3f));
            g.drawOval(x + w / 2 - 230, y + h / 2 - 230, 460, 460);

            // Diamond center emblem
            int cx = x + w / 2;
            int cy = y + h / 2;
            Polygon p = new Polygon(
                    new int[]{cx, cx + 70, cx, cx - 70},
                    new int[]{cy - 70, cy, cy + 70, cy},
                    4
            );
            g.setColor(primary);
            g.fillPolygon(p);
            g.setColor(Color.WHITE);
            g.drawPolygon(p);
        }

        if (caption != null && !caption.trim().isEmpty()) {
            g.setColor(new Color(15, 23, 42, 195));
            g.fillRect(x, y + h - 54, w, 54);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 19));
            g.drawString(truncate(caption.trim(), 52), x + 24, y + h - 20);
        }

        g.setClip(prevClip);
    }

    private void drawSlideFooter(
            Graphics2D g,
            int pageNum,
            int totalPages,
            String mainTitle,
            Color primary,
            Color secondary,
            String layout
    ) {
        boolean darkSlide = "closing-thankyou".equals(layout);
        Color textColor = darkSlide ? new Color(203, 213, 225) : new Color(100, 116, 139);

        // Page badge at bottom right
        g.setColor(primary);
        g.fillRoundRect(WIDTH - 135, HEIGHT - 68, 95, 44, 12, 12);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        String pageStr = String.format("%02d/%02d", pageNum, Math.max(pageNum, totalPages));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(pageStr, WIDTH - 135 + (95 - fm.stringWidth(pageStr)) / 2, HEIGHT - 39);

        if (!"cover-circle".equals(layout) && !"closing-thankyou".equals(layout) && !"infographic-stats".equals(layout)) {
            g.setColor(textColor);
            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            g.drawString(truncate(mainTitle.toUpperCase(), 60), 100, HEIGHT - 38);
        }
    }

    private int drawWrappedText(Graphics2D g, String text, int x, int y, int maxWidth, int lineHeight, int maxLines) {
        if (text == null || text.trim().isEmpty()) {
            return y;
        }
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.trim().split("\\s+");
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (fm.stringWidth(candidate) <= maxWidth) {
                currentLine = new StringBuilder(candidate);
            } else {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                }
                currentLine = new StringBuilder(word);
            }
        }
        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        int count = Math.min(lines.size(), maxLines);
        int currY = y;
        for (int i = 0; i < count; i++) {
            String line = lines.get(i);
            if (i == maxLines - 1 && lines.size() > maxLines && line.length() > 3) {
                line = line.substring(0, Math.max(1, line.length() - 3)) + "...";
            }
            g.drawString(line, x, currY);
            currY += lineHeight;
        }
        return currY;
    }

    private BufferedImage decodeBase64Photo(String base64) {
        if (base64 == null || base64.trim().isEmpty()) {
            return null;
        }
        try {
            String clean = base64.trim();
            int commaIdx = clean.indexOf(',');
            if (commaIdx >= 0) {
                clean = clean.substring(commaIdx + 1);
            }
            byte[] bytes = Base64.getDecoder().decode(clean);
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
                return ImageIO.read(bis);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private Color parseColor(String hex, Color fallback) {
        if (hex == null || hex.trim().isEmpty()) {
            return fallback;
        }
        try {
            String clean = hex.trim();
            if (clean.startsWith("#")) {
                clean = clean.substring(1);
            }
            if (clean.length() == 6) {
                int r = Integer.parseInt(clean.substring(0, 2), 16);
                int g = Integer.parseInt(clean.substring(2, 4), 16);
                int b = Integer.parseInt(clean.substring(4, 6), 16);
                return new Color(r, g, b);
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private Color blend(Color c1, Color c2, float ratio) {
        float ir = 1.0f - ratio;
        int r = Math.min(255, Math.max(0, (int) (c1.getRed() * ir + c2.getRed() * ratio)));
        int g = Math.min(255, Math.max(0, (int) (c1.getGreen() * ir + c2.getGreen() * ratio)));
        int b = Math.min(255, Math.max(0, (int) (c1.getBlue() * ir + c2.getBlue() * ratio)));
        return new Color(r, g, b);
    }

    private String safe(String val, String fallback) {
        return (val != null && !val.trim().isEmpty()) ? val.trim() : fallback;
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 3) + "...";
    }
}

