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
            case "cover-minimalist" -> renderCoverMinimalist(g, slide, primary, secondary, userPhoto);
            case "cover-banner-hero" -> renderCoverBannerHero(g, slide, primary, secondary, userPhoto);
            case "split-photo-left" -> renderSplitPhotoLeft(g, slide, primary, secondary, userPhoto);
            case "split-photo-right" -> renderSplitPhotoRight(g, slide, primary, secondary, userPhoto);
            case "photo-full-banner" -> renderPhotoFullBanner(g, slide, primary, secondary, userPhoto);
            case "card-photo-duo" -> renderCardPhotoDuo(g, slide, primary, secondary, userPhoto);
            case "columns-4" -> renderColumns4(g, slide, primary, secondary, userPhoto);
            case "grid-4-cards" -> renderGrid4Cards(g, slide, primary, secondary, userPhoto);
            case "columns-3-cards" -> renderColumns3Cards(g, slide, primary, secondary, userPhoto);
            case "table-comparison" -> renderTableComparison(g, slide, primary, secondary, userPhoto);
            case "infographic-stats" -> renderInfographicStats(g, slide, primary, secondary, userPhoto);
            case "timeline-steps" -> renderTimelineSteps(g, slide, primary, secondary, userPhoto);
            case "process-horizontal-3" -> renderProcessHorizontal3(g, slide, primary, secondary, userPhoto);
            case "big-numbers-stats" -> renderBigNumbersStats(g, slide, primary, secondary, userPhoto);
            case "quote-vision" -> renderQuoteVision(g, slide, primary, secondary, userPhoto);
            case "concept-focus-circle" -> renderConceptFocusCircle(g, slide, primary, secondary, userPhoto);
            case "closing-thankyou" -> renderClosingThankYou(g, slide, primary, secondary, userPhoto);
            case "closing-minimal-dark" -> renderClosingMinimalDark(g, slide, primary, secondary, userPhoto);
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

        // Circular Photo Frame shifted right to never overlap text
        int cx = 1340, cy = 540, r = 340;
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
        y = drawWrappedText(g, safe(slide.getBodyText(), ""), rx, y + 25, 940, 36, 3);

        // Bullets with numbered badges
        List<String> bullets = slide.getBullets();
        if (bullets != null && !bullets.isEmpty()) {
            int by = Math.min(Math.max(y + 25, 600), 730);
            for (int i = 0; i < Math.min(3, bullets.size()); i++) {
                g.setColor(primary);
                g.fillRoundRect(rx, by - 32, 48, 42, 10, 10);
                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, 20));
                g.drawString("0" + (i + 1), rx + 12, by - 4);

                g.setColor(secondary);
                g.setFont(new Font("SansSerif", Font.PLAIN, 23));
                drawWrappedText(g, bullets.get(i), rx + 68, by - 4, 870, 30, 2);
                by += 72;
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
        y = drawWrappedText(g, safe(slide.getBodyText(), ""), lx, y + 45, 900, 36, 3);

        // Highlight cards on left bottom
        List<String> bullets = slide.getBullets();
        if (bullets != null && !bullets.isEmpty()) {
            int by = Math.min(Math.max(y + 25, 580), 650);
            for (int i = 0; i < Math.min(3, bullets.size()); i++) {
                g.setColor(new Color(248, 250, 252));
                g.fillRoundRect(lx, by, 900, 80, 14, 14);
                g.setColor(i % 2 == 0 ? primary : secondary);
                g.fillRoundRect(lx, by, 12, 80, 6, 6);

                g.setColor(secondary);
                g.setFont(new Font("SansSerif", Font.BOLD, 22));
                drawWrappedText(g, bullets.get(i), lx + 34, by + 34, 840, 26, 2);
                by += 96;
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

    // 11. COVER MINIMALIST
    private void renderCoverMinimalist(Graphics2D g, SlidePageDto slide, Color primary, Color secondary, BufferedImage photo) {
        g.setColor(primary);
        g.fillRect(0, 0, 36, HEIGHT);

        g.setColor(primary);
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("●  APRESENTAÇÃO OFICIAL", 120, 160);

        g.setColor(secondary);
        g.setFont(new Font("SansSerif", Font.BOLD, 64));
        int nextY = drawWrappedText(g, safe(slide.getTitle(), "TÍTULO PRINCIPAL"), 120, 270, 950, 76, 3);

        g.setColor(primary);
        g.fillRect(120, nextY + 12, 120, 8);

        g.setColor(new Color(71, 85, 105));
        g.setFont(new Font("SansSerif", Font.PLAIN, 28));
        nextY = drawWrappedText(g, safe(slide.getSubtitle(), ""), 120, nextY + 62, 920, 40, 2);

        List<String> bullets = slide.getBullets();
        if (bullets != null && !bullets.isEmpty()) {
            int by = Math.min(nextY + 50, 720);
            for (int i = 0; i < Math.min(3, bullets.size()); i++) {
                g.setColor(primary);
                g.fillOval(120, by - 24, 30, 30);
                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, 18));
                g.drawString("✓", 128, by - 2);

                g.setColor(secondary);
                g.setFont(new Font("SansSerif", Font.BOLD, 24));
                drawWrappedText(g, bullets.get(i), 168, by - 2, 860, 32, 2);
                by += 68;
            }
        }

        int px = 1140, py = 150, pw = 680, ph = 780;
        g.setColor(new Color(248, 250, 252));
        g.fillRoundRect(px - 14, py - 14, pw + 28, ph + 28, 24, 24);
        g.setColor(new Color(226, 232, 240));
        g.setStroke(new BasicStroke(3f));
        g.drawRoundRect(px - 14, py - 14, pw + 28, ph + 28, 24, 24);
        drawPhotoOrPlaceholder(g, photo, px, py, pw, ph, primary, secondary, slide.getPhotoCaption());
    }

    // 12. COVER BANNER HERO
    private void renderCoverBannerHero(Graphics2D g, SlidePageDto slide, Color primary, Color secondary, BufferedImage photo) {
        g.setColor(primary);
        g.fillRect(0, 0, WIDTH, 600);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("SLIDES DIDÁTICOS • VISÃO GERAL", 120, 150);

        g.setFont(new Font("SansSerif", Font.BOLD, 64));
        int nextY = drawWrappedText(g, safe(slide.getTitle(), "TÍTULO DA APRESENTAÇÃO"), 120, 260, 1050, 76, 2);

        g.setColor(new Color(255, 255, 255, 235));
        g.setFont(new Font("SansSerif", Font.PLAIN, 28));
        drawWrappedText(g, safe(slide.getSubtitle(), ""), 120, nextY + 60, 1050, 40, 2);

        // Circular photo badge on right of hero banner
        int cx = 1540, cy = 300, r = 200;
        g.setColor(Color.WHITE);
        g.fillOval(cx - r - 8, cy - r - 8, (r + 8) * 2, (r + 8) * 2);
        Shape oldClip = g.getClip();
        Ellipse2D circleClip = new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2);
        g.setClip(circleClip);
        drawPhotoOrPlaceholder(g, photo, cx - r, cy - r, r * 2, r * 2, primary, secondary, null);
        g.setClip(oldClip);

        // Lower cards area
        g.setColor(new Color(248, 250, 252));
        g.fillRect(0, 600, WIDTH, 480);

        List<String> bullets = slide.getBullets();
        if (bullets == null || bullets.isEmpty()) {
            bullets = List.of("Conceito fundamental e bases", "Aplicações práticas no cotidiano", "Síntese dos principais resultados");
        }
        for (int i = 0; i < Math.min(3, bullets.size()); i++) {
            int cxBox = 120 + i * 580;
            int cyBox = 670;
            int cw = 520, ch = 240;

            g.setColor(Color.WHITE);
            g.fillRoundRect(cxBox, cyBox, cw, ch, 16, 16);
            g.setColor(new Color(226, 232, 240));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(cxBox, cyBox, cw, ch, 16, 16);

            g.setColor(i % 2 == 0 ? primary : secondary);
            g.fillRect(cxBox, cyBox, cw, 8);

            g.setColor(primary);
            g.setFont(new Font("SansSerif", Font.BOLD, 36));
            g.drawString("0" + (i + 1), cxBox + 24, cyBox + 60);

            g.setColor(new Color(30, 41, 59));
            g.setFont(new Font("SansSerif", Font.PLAIN, 24));
            drawWrappedText(g, bullets.get(i), cxBox + 24, cyBox + 110, cw - 48, 34, 3);
        }
    }

    // 13. PHOTO FULL BANNER
    private void renderPhotoFullBanner(Graphics2D g, SlidePageDto slide, Color primary, Color secondary, BufferedImage photo) {
        int bannerH = 460;
        drawPhotoOrPlaceholder(g, photo, 0, 0, WIDTH, bannerH, primary, secondary, null);

        // Semi-transparent gradient overlay for text readability
        g.setColor(new Color(secondary.getRed(), secondary.getGreen(), secondary.getBlue(), 220));
        g.fillRect(0, 0, WIDTH, bannerH);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 54));
        int nextY = drawWrappedText(g, safe(slide.getTitle(), "TÍTULO DA SEÇÃO"), 120, 210, 1680, 64, 2);

        g.setColor(new Color(255, 255, 255, 230));
        g.setFont(new Font("SansSerif", Font.PLAIN, 28));
        drawWrappedText(g, safe(slide.getSubtitle(), ""), 120, nextY + 54, 1680, 38, 2);

        g.setColor(primary);
        g.fillRect(0, bannerH - 12, WIDTH, 12);

        // Bottom 3 structured cards
        List<SlidePageDto.ColumnItemDto> cols = slide.getColumns();
        for (int i = 0; i < 3; i++) {
            int cx = 120 + i * 580;
            int cy = bannerH + 50;
            int cw = 520, ch = 430;

            g.setColor(Color.WHITE);
            g.fillRoundRect(cx, cy, cw, ch, 16, 16);
            g.setColor(new Color(226, 232, 240));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(cx, cy, cw, ch, 16, 16);

            Color headerColor = i % 2 == 0 ? primary : secondary;
            g.setColor(headerColor);
            g.fillRoundRect(cx + 24, cy + 28, 48, 40, 8, 8);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 22));
            g.drawString("0" + (i + 1), cx + 38, cy + 56);

            String cTitle = (cols != null && i < cols.size() && cols.get(i).getTitle() != null)
                    ? cols.get(i).getTitle() : ("Tópico 0" + (i + 1));
            String cDesc = (cols != null && i < cols.size() && cols.get(i).getDescription() != null)
                    ? cols.get(i).getDescription() : "Fundamentos e metodologia de estudo prático.";

            g.setColor(secondary);
            g.setFont(new Font("SansSerif", Font.BOLD, 26));
            drawWrappedText(g, cTitle, cx + 86, cy + 56, cw - 100, 32, 1);

            g.setColor(new Color(226, 232, 240));
            g.drawLine(cx + 24, cy + 86, cx + cw - 24, cy + 86);

            g.setColor(new Color(71, 85, 105));
            g.setFont(new Font("SansSerif", Font.PLAIN, 23));
            drawWrappedText(g, cDesc, cx + 24, cy + 130, cw - 48, 34, 6);
        }
    }

    // 14. CARD PHOTO DUO
    private void renderCardPhotoDuo(Graphics2D g, SlidePageDto slide, Color primary, Color secondary, BufferedImage photo) {
        g.setColor(secondary);
        g.setFont(new Font("SansSerif", Font.BOLD, 48));
        int y = drawWrappedText(g, safe(slide.getTitle(), "TÍTULO CONCEITUAL"), 120, 140, 1680, 58, 2);

        g.setColor(primary);
        g.fillRect(120, y + 10, 120, 8);

        g.setColor(new Color(71, 85, 105));
        g.setFont(new Font("SansSerif", Font.PLAIN, 26));
        y = drawWrappedText(g, safe(slide.getSubtitle(), ""), 120, y + 54, 1680, 36, 1);

        // Left text column (w = 860)
        int lx = 120, ly = y + 40;
        g.setColor(new Color(51, 65, 85));
        g.setFont(new Font("SansSerif", Font.PLAIN, 24));
        ly = drawWrappedText(g, safe(slide.getBodyText(), "Explicação detalhada dos conceitos apresentados com exemplos."), lx, ly, 860, 36, 4);

        List<String> bullets = slide.getBullets();
        if (bullets != null && !bullets.isEmpty()) {
            int by = Math.min(ly + 30, 680);
            for (int i = 0; i < Math.min(2, bullets.size()); i++) {
                g.setColor(new Color(248, 250, 252));
                g.fillRoundRect(lx, by, 860, 90, 12, 12);
                g.setColor(i == 0 ? primary : secondary);
                g.fillRoundRect(lx, by, 10, 90, 4, 4);

                g.setColor(secondary);
                g.setFont(new Font("SansSerif", Font.BOLD, 23));
                drawWrappedText(g, "▸ " + bullets.get(i), lx + 30, by + 40, 810, 30, 2);
                by += 110;
            }
        }

        // Right photo card (w = 760, h = 680)
        int px = 1040, py = 230, pw = 760, ph = 680;
        g.setColor(Color.WHITE);
        g.fillRoundRect(px - 10, py - 10, pw + 20, ph + 20, 20, 20);
        g.setColor(primary);
        g.setStroke(new BasicStroke(3f));
        g.drawRoundRect(px - 10, py - 10, pw + 20, ph + 20, 20, 20);
        drawPhotoOrPlaceholder(g, photo, px, py, pw, ph, primary, secondary, slide.getPhotoCaption());
    }

    // 15. COLUMNS 3 CARDS
    private void renderColumns3Cards(Graphics2D g, SlidePageDto slide, Color primary, Color secondary, BufferedImage photo) {
        g.setColor(secondary);
        g.fillRect(0, 0, WIDTH, 240);
        g.setColor(primary);
        g.fillRect(0, 240, WIDTH, 12);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 50));
        drawWrappedText(g, safe(slide.getTitle(), "PILARES PRINCIPAIS"), 120, 130, 1400, 58, 1);

        g.setColor(new Color(255, 255, 255, 220));
        g.setFont(new Font("SansSerif", Font.PLAIN, 26));
        g.drawString(truncate(safe(slide.getSubtitle(), ""), 90), 120, 200);

        if (photo != null) {
            drawPhotoOrPlaceholder(g, photo, 1600, 30, 220, 160, primary, secondary, null);
        }

        List<SlidePageDto.ColumnItemDto> cols = slide.getColumns();
        for (int i = 0; i < 3; i++) {
            boolean isCenter = (i == 1);
            int cx = 120 + i * 580;
            int cy = 290;
            int cw = 520, ch = 660;

            Color cardBg = isCenter ? primary : new Color(248, 250, 252);
            Color titleColor = isCenter ? Color.WHITE : secondary;
            Color textColor = isCenter ? new Color(255, 255, 255, 235) : new Color(71, 85, 105);

            g.setColor(cardBg);
            g.fillRoundRect(cx, cy, cw, ch, 20, 20);
            g.setColor(new Color(226, 232, 240));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(cx, cy, cw, ch, 20, 20);

            // Badge
            Color badgeBg = isCenter ? secondary : primary;
            g.setColor(badgeBg);
            g.fillRoundRect(cx + 30, cy + 34, 60, 40, 10, 10);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 22));
            g.drawString("0" + (i + 1), cx + 46, cy + 62);

            String cTitle = (cols != null && i < cols.size() && cols.get(i).getTitle() != null)
                    ? cols.get(i).getTitle() : ("Pilar 0" + (i + 1));
            String cDesc = (cols != null && i < cols.size() && cols.get(i).getDescription() != null)
                    ? cols.get(i).getDescription() : "Fundamentos teóricos e desdobramentos práticos essenciais.";

            g.setColor(titleColor);
            g.setFont(new Font("SansSerif", Font.BOLD, 30));
            int ny = drawWrappedText(g, cTitle, cx + 30, cy + 130, cw - 60, 36, 2);

            g.setColor(isCenter ? new Color(255, 255, 255, 120) : primary);
            g.fillRect(cx + 30, ny + 10, 60, 5);

            g.setColor(textColor);
            g.setFont(new Font("SansSerif", Font.PLAIN, 24));
            drawWrappedText(g, cDesc, cx + 30, ny + 50, cw - 60, 36, 8);
        }
    }

    // 16. TABLE COMPARISON
    private void renderTableComparison(Graphics2D g, SlidePageDto slide, Color primary, Color secondary, BufferedImage photo) {
        g.setColor(secondary);
        g.setFont(new Font("SansSerif", Font.BOLD, 48));
        int y = drawWrappedText(g, safe(slide.getTitle(), "QUADRO COMPARATIVO"), 120, 140, 1680, 58, 2);

        g.setColor(primary);
        g.fillRect(120, y + 10, 100, 8);

        g.setColor(new Color(71, 85, 105));
        g.setFont(new Font("SansSerif", Font.PLAIN, 26));
        drawWrappedText(g, safe(slide.getSubtitle(), ""), 120, y + 54, 1680, 36, 1);

        int panelY = 240;
        int panelW = 810;
        int panelH = 700;

        // Left Panel (Primary Header)
        g.setColor(new Color(248, 250, 252));
        g.fillRoundRect(120, panelY, panelW, panelH, 20, 20);
        g.setColor(primary);
        g.fillRoundRect(120, panelY, panelW, 70, 20, 20);
        g.fillRect(120, panelY + 40, panelW, 30);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 26));
        String titleA = (slide.getColumns() != null && !slide.getColumns().isEmpty() && slide.getColumns().get(0).getTitle() != null)
                ? slide.getColumns().get(0).getTitle() : "VISÃO GERAL / TÓPICO A";
        g.drawString(truncate(titleA.toUpperCase(), 35), 150, panelY + 45);

        List<String> bulletsA = slide.getBullets();
        if (bulletsA == null || bulletsA.isEmpty()) {
            bulletsA = List.of("Fundamentos teóricos e conceitos-base", "Aplicações práticas e resolução de exemplos", "Metodologia estruturada e eficiência");
        }
        int byA = panelY + 120;
        for (int i = 0; i < Math.min(3, bulletsA.size()); i++) {
            g.setColor(primary);
            g.setFont(new Font("SansSerif", Font.BOLD, 26));
            g.drawString("✔", 150, byA + 10);
            g.setColor(new Color(30, 41, 59));
            g.setFont(new Font("SansSerif", Font.PLAIN, 24));
            drawWrappedText(g, bulletsA.get(i), 190, byA + 10, panelW - 90, 34, 3);
            byA += 130;
        }

        // Right Panel (Secondary Header)
        int rx = 990;
        g.setColor(new Color(248, 250, 252));
        g.fillRoundRect(rx, panelY, panelW, panelH, 20, 20);
        g.setColor(secondary);
        g.fillRoundRect(rx, panelY, panelW, 70, 20, 20);
        g.fillRect(rx, panelY + 40, panelW, 30);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 26));
        String titleB = (slide.getColumns() != null && slide.getColumns().size() > 1 && slide.getColumns().get(1).getTitle() != null)
                ? slide.getColumns().get(1).getTitle() : "ANÁLISE COMPARATIVA / TÓPICO B";
        g.drawString(truncate(titleB.toUpperCase(), 35), rx + 30, panelY + 45);

        int byB = panelY + 120;
        List<String> bulletsB = (bulletsA.size() > 3) ? bulletsA.subList(3, Math.min(6, bulletsA.size()))
                : List.of("Aprofundamento e estudos de caso", "Análise comparativa de desfechos", "Resultados consolidados e conclusões");
        for (int i = 0; i < Math.min(3, bulletsB.size()); i++) {
            g.setColor(secondary);
            g.setFont(new Font("SansSerif", Font.BOLD, 26));
            g.drawString("✔", rx + 30, byB + 10);
            g.setColor(new Color(30, 41, 59));
            g.setFont(new Font("SansSerif", Font.PLAIN, 24));
            drawWrappedText(g, bulletsB.get(i), rx + 70, byB + 10, panelW - 90, 34, 3);
            byB += 130;
        }
    }

    // 17. PROCESS HORIZONTAL 3
    private void renderProcessHorizontal3(Graphics2D g, SlidePageDto slide, Color primary, Color secondary, BufferedImage photo) {
        g.setColor(primary);
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("FLUXO DE ETAPAS • PROCESSO", 120, 130);

        g.setColor(secondary);
        g.setFont(new Font("SansSerif", Font.BOLD, 48));
        int y = drawWrappedText(g, safe(slide.getTitle(), "PROCESSO PASSO A PASSO"), 120, 210, 1680, 58, 2);

        g.setColor(primary);
        g.fillRect(120, y + 10, 100, 8);

        g.setColor(new Color(71, 85, 105));
        g.setFont(new Font("SansSerif", Font.PLAIN, 26));
        drawWrappedText(g, safe(slide.getSubtitle(), ""), 120, y + 54, 1680, 36, 1);

        // Connecting dashed line
        g.setColor(new Color(secondary.getRed(), secondary.getGreen(), secondary.getBlue(), 50));
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{12, 12}, 0));
        g.drawLine(380, 480, 1540, 480);
        g.setStroke(new BasicStroke(1f));

        List<SlidePageDto.ColumnItemDto> cols = slide.getColumns();
        for (int i = 0; i < 3; i++) {
            int cx = 120 + i * 580;
            int cw = 520;
            int nodeX = cx + cw / 2;
            int nodeY = 480;

            // Number node circle
            Color nodeColor = i % 2 == 0 ? primary : secondary;
            g.setColor(nodeColor);
            g.fillOval(nodeX - 44, nodeY - 44, 88, 88);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(5f));
            g.drawOval(nodeX - 44, nodeY - 44, 88, 88);
            g.setFont(new Font("SansSerif", Font.BOLD, 36));
            FontMetrics fm = g.getFontMetrics();
            String nStr = "0" + (i + 1);
            g.drawString(nStr, nodeX - fm.stringWidth(nStr) / 2, nodeY + 12);

            // Step card
            int cy = 560, ch = 380;
            g.setColor(new Color(248, 250, 252));
            g.fillRoundRect(cx, cy, cw, ch, 16, 16);
            g.setColor(new Color(226, 232, 240));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(cx, cy, cw, ch, 16, 16);

            String cTitle = (cols != null && i < cols.size() && cols.get(i).getTitle() != null)
                    ? cols.get(i).getTitle() : ("Fase 0" + (i + 1));
            String cDesc = (cols != null && i < cols.size() && cols.get(i).getDescription() != null)
                    ? cols.get(i).getDescription() : "Definição de objetivos, execução metodológica e validação.";

            g.setColor(secondary);
            g.setFont(new Font("SansSerif", Font.BOLD, 28));
            drawWrappedText(g, cTitle, cx + 24, cy + 60, cw - 48, 34, 2);

            g.setColor(new Color(71, 85, 105));
            g.setFont(new Font("SansSerif", Font.PLAIN, 23));
            drawWrappedText(g, cDesc, cx + 24, cy + 130, cw - 48, 34, 6);

            // Arrow
            if (i < 2) {
                g.setColor(primary);
                g.setFont(new Font("SansSerif", Font.BOLD, 36));
                g.drawString("➔", cx + cw + 15, nodeY + 12);
            }
        }
    }

    // 18. BIG NUMBERS STATS
    private void renderBigNumbersStats(Graphics2D g, SlidePageDto slide, Color primary, Color secondary, BufferedImage photo) {
        // Left primary column
        int colW = 760;
        g.setColor(primary);
        g.fillRect(0, 0, colW, HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 54));
        int nextY = drawWrappedText(g, safe(slide.getTitle(), "MÉTRICAS & INDICADORES"), 100, 260, colW - 160, 64, 3);

        g.setColor(secondary);
        g.fillRect(100, nextY + 12, 100, 8);

        g.setColor(new Color(255, 255, 255, 235));
        g.setFont(new Font("SansSerif", Font.PLAIN, 28));
        nextY = drawWrappedText(g, safe(slide.getSubtitle(), ""), 100, nextY + 60, colW - 160, 40, 3);

        g.setColor(new Color(255, 255, 255, 210));
        g.setFont(new Font("SansSerif", Font.PLAIN, 24));
        drawWrappedText(g, safe(slide.getBodyText(), ""), 100, nextY + 30, colW - 160, 36, 4);

        // Right side 3 stat cards
        List<SlidePageDto.StatItemDto> stats = slide.getStats();
        int rx = 860, rw = 940;
        int startY = 140;
        for (int i = 0; i < 3; i++) {
            int cy = startY + i * 270;
            int ch = 220;

            g.setColor(new Color(248, 250, 252));
            g.fillRoundRect(rx, cy, rw, ch, 16, 16);
            g.setColor(new Color(226, 232, 240));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(rx, cy, rw, ch, 16, 16);

            String val = (stats != null && i < stats.size() && stats.get(i).getValue() != null)
                    ? stats.get(i).getValue() : ("0" + (i + 1));
            String label = (stats != null && i < stats.size() && stats.get(i).getLabel() != null)
                    ? stats.get(i).getLabel() : ("Indicador 0" + (i + 1));
            String desc = (stats != null && i < stats.size() && stats.get(i).getDescription() != null)
                    ? stats.get(i).getDescription() : "Resultados expressivos e evolução contínua.";

            // Big number
            g.setColor(i % 2 == 0 ? primary : secondary);
            g.setFont(new Font("SansSerif", Font.BOLD, 76));
            g.drawString(val, rx + 40, cy + 130);

            // Label & description
            g.setColor(secondary);
            g.setFont(new Font("SansSerif", Font.BOLD, 30));
            drawWrappedText(g, label, rx + 240, cy + 60, rw - 270, 36, 1);

            g.setColor(new Color(71, 85, 105));
            g.setFont(new Font("SansSerif", Font.PLAIN, 23));
            drawWrappedText(g, desc, rx + 240, cy + 110, rw - 270, 32, 3);
        }
    }

    // 19. CONCEPT FOCUS CIRCLE
    private void renderConceptFocusCircle(Graphics2D g, SlidePageDto slide, Color primary, Color secondary, BufferedImage photo) {
        g.setColor(secondary);
        g.setFont(new Font("SansSerif", Font.BOLD, 48));
        drawWrappedText(g, safe(slide.getTitle(), "CONCEITO CENTRAL"), 120, 110, 1680, 56, 1);

        g.setColor(new Color(71, 85, 105));
        g.setFont(new Font("SansSerif", Font.PLAIN, 24));
        g.drawString(truncate(safe(slide.getSubtitle(), ""), 90), 120, 160);

        List<SlidePageDto.ColumnItemDto> cols = slide.getColumns();

        // 2 Left cards
        for (int i = 0; i < 2; i++) {
            int cx = 120, cy = 250 + i * 320, cw = 540, ch = 280;
            g.setColor(new Color(248, 250, 252));
            g.fillRoundRect(cx, cy, cw, ch, 16, 16);
            g.setColor(primary);
            g.fillRoundRect(cx, cy, 12, ch, 6, 6);

            String cTitle = (cols != null && i < cols.size() && cols.get(i).getTitle() != null)
                    ? cols.get(i).getTitle() : ("Fundamento 0" + (i + 1));
            String cDesc = (cols != null && i < cols.size() && cols.get(i).getDescription() != null)
                    ? cols.get(i).getDescription() : "Estrutura teórica e base conceitual aplicada ao tema.";

            g.setColor(secondary);
            g.setFont(new Font("SansSerif", Font.BOLD, 28));
            drawWrappedText(g, cTitle, cx + 34, cy + 50, cw - 50, 34, 1);

            g.setColor(new Color(71, 85, 105));
            g.setFont(new Font("SansSerif", Font.PLAIN, 23));
            drawWrappedText(g, cDesc, cx + 34, cy + 110, cw - 50, 32, 4);
        }

        // Center hub circle
        int hubX = 960, hubY = 560, r = 180;
        g.setColor(secondary);
        g.fillOval(hubX - r, hubY - r, r * 2, r * 2);
        g.setColor(primary);
        g.setStroke(new BasicStroke(8f));
        g.drawOval(hubX - r, hubY - r, r * 2, r * 2);

        int ir = 130;
        g.setColor(primary);
        g.fillOval(hubX - ir, hubY - ir, ir * 2, ir * 2);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        String hubTxt = safe(slide.getTitle(), "CONCEITO");
        drawWrappedText(g, hubTxt, hubX - ir + 20, hubY - 20, ir * 2 - 40, 34, 3);

        // 2 Right cards
        for (int i = 0; i < 2; i++) {
            int cxR = 1260, cy = 250 + i * 320, cw = 540, ch = 280;
            g.setColor(new Color(248, 250, 252));
            g.fillRoundRect(cxR, cy, cw, ch, 16, 16);
            g.setColor(secondary);
            g.fillRoundRect(cxR + cw - 12, cy, 12, ch, 6, 6);

            String cTitle = (cols != null && (i + 2) < cols.size() && cols.get(i + 2).getTitle() != null)
                    ? cols.get(i + 2).getTitle() : ("Aplicação 0" + (i + 3));
            String cDesc = (cols != null && (i + 2) < cols.size() && cols.get(i + 2).getDescription() != null)
                    ? cols.get(i + 2).getDescription() : "Resultados práticos observados na aplicação do método.";

            g.setColor(secondary);
            g.setFont(new Font("SansSerif", Font.BOLD, 28));
            drawWrappedText(g, cTitle, cxR + 24, cy + 50, cw - 50, 34, 1);

            g.setColor(new Color(71, 85, 105));
            g.setFont(new Font("SansSerif", Font.PLAIN, 23));
            drawWrappedText(g, cDesc, cxR + 24, cy + 110, cw - 50, 32, 4);
        }
    }

    // 20. CLOSING MINIMAL DARK
    private void renderClosingMinimalDark(Graphics2D g, SlidePageDto slide, Color primary, Color secondary, BufferedImage photo) {
        g.setColor(secondary);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Left accent stripe
        g.setColor(primary);
        g.fillRect(0, 0, 24, HEIGHT);

        g.setColor(primary);
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("SÍNTESE & CONCLUSÃO", 120, 160);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 68));
        String title = safe(slide.getTitle(), "MUITO OBRIGADO!");
        int nextY = drawWrappedText(g, title, 120, 260, 950, 78, 2);

        g.setColor(primary);
        g.fillRect(120, nextY + 12, 120, 8);

        g.setColor(new Color(255, 255, 255, 235));
        g.setFont(new Font("SansSerif", Font.PLAIN, 28));
        nextY = drawWrappedText(g, safe(slide.getSubtitle(), "Apresentação desenvolvida com clareza e método."), 120, nextY + 62, 920, 40, 2);

        List<String> bullets = slide.getBullets();
        if (bullets != null && !bullets.isEmpty()) {
            int by = Math.min(nextY + 50, 720);
            for (int i = 0; i < Math.min(3, bullets.size()); i++) {
                g.setColor(primary);
                g.setFont(new Font("SansSerif", Font.BOLD, 26));
                g.drawString("✔", 120, by + 10);

                g.setColor(new Color(203, 213, 225));
                g.setFont(new Font("SansSerif", Font.PLAIN, 24));
                drawWrappedText(g, bullets.get(i), 160, by + 10, 880, 32, 2);
                by += 68;
            }
        }

        // Right completion box
        int px = 1160, py = 220, pw = 640, ph = 620;
        g.setColor(new Color(255, 255, 255, 10));
        g.fillRoundRect(px, py, pw, ph, 20, 20);
        g.setColor(primary);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(px, py, pw, ph, 20, 20);

        g.setColor(primary);
        g.fillRoundRect(px, py, pw, 70, 20, 20);
        g.fillRect(px, py + 40, pw, 30);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 26));
        g.drawString("RESUMO FINAL", px + 40, py + 46);

        g.setColor(new Color(203, 213, 225));
        g.setFont(new Font("SansSerif", Font.PLAIN, 24));
        drawWrappedText(g, "Perguntas, dúvidas e feedback são bem-vindos! Obrigado pela atenção.", px + 40, py + 140, pw - 80, 36, 4);

        g.setColor(new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), 50));
        g.fillRoundRect(px + 40, py + 340, 320, 50, 25, 25);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.drawString("Concluído com Sucesso", px + 80, py + 372);
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
        boolean darkSlide = "closing-thankyou".equals(layout) || "closing-minimal-dark".equals(layout);
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

