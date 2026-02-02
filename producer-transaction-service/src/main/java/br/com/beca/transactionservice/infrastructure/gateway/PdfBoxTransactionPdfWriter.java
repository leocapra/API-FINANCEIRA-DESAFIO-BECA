package br.com.beca.transactionservice.infrastructure.gateway;

import br.com.beca.transactionservice.application.port.TransactionPdfWriterPort;
import br.com.beca.transactionservice.domain.model.Transaction;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PdfBoxTransactionPdfWriter implements TransactionPdfWriterPort {

    // ---- Página Landscape ----
    private static final PDRectangle A4_LANDSCAPE =
            new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());

    // ---- Layout ----
    private static final float MARGIN = 22f;
    private static final float HEADER_SPACING = 10f;
    private static final float SECTION_TITLE_H = 18f;
    private static final float TABLE_HEADER_H = 20f;
    private static final float LINE_SPACING = 12f;
    private static final float ROW_MIN_H = 14f;
    private static final float FOOTER = 22f;

    // ---- Zebra ----
    private static final Color ZEBRA_LIGHT = new Color(246, 246, 246);

    // ---- Fonte ----
    private static final String DEFAULT_TTF = "/fonts/NotoSans-Regular.ttf";
    private static final float FONT_SIZE = 8f;
    private static final float FONT_SIZE_TITLE = 12f;

    // ---- Alinhamento e quebra ----
    private enum Align { LEFT, CENTER, RIGHT }
    private enum BreakMode { ELLIPSIS } // todas as células 1 linha, truncadas com “…”

    private static final class Col {
        final String header;
        final float rel;         // largura relativa (normalizada depois)
        final Align align;
        final BreakMode mode;
        final int maxCharsHint;  // dica de truncamento para UX
        final Function<Transaction, String> valueFn;

        Col(String header, float rel, Align align, BreakMode mode, int maxCharsHint,
            Function<Transaction, String> valueFn) {
            this.header = header;
            this.rel = rel;
            this.align = align;
            this.mode = mode;
            this.maxCharsHint = maxCharsHint;
            this.valueFn = valueFn;
        }
    }

    // ---- Formatação ----
    private final Locale ptBR = new Locale("pt", "BR");
    private final NumberFormat currencyFmt = NumberFormat.getCurrencyInstance(ptBR);
    private final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // duas casas para BRL e CAMBIO (FX)
    private final DecimalFormat twoDec;
    {
        DecimalFormatSymbols sym = new DecimalFormatSymbols(ptBR);
        sym.setDecimalSeparator(',');
        sym.setGroupingSeparator('.');
        twoDec = new DecimalFormat("0.00", sym);
        twoDec.setGroupingUsed(false);
    }

    // ---- Tipos (ajuste nomes conforme seu domínio) ----
    private static final String TIPO_DEPOSITO      = "DEPOSITO";
    private static final String TIPO_SAQUE         = "SAQUE";
    private static final String TIPO_TRANSFERENCIA = "TRANSFERENCIA";
    private static final String TIPO_COMPRA        = "COMPRA";

    // =====================================================================
    // ENTRYPOINT
    // =====================================================================
    @Override
    public byte[] write(List<Transaction> all) throws IOException {
        // Agrupa por tipo (categoria)
        Map<String, List<Transaction>> porTipo = all.stream()
                .collect(Collectors.groupingBy(t -> safe(enumOrString(t.getType()))));

        // Ordem de seções
        List<String> ordem = List.of(TIPO_DEPOSITO, TIPO_SAQUE, TIPO_TRANSFERENCIA, TIPO_COMPRA);

        try (PDDocument doc = new PDDocument()) {
            var fontBody = tryLoadUnicodeFont(doc, DEFAULT_TTF); // Unicode/acentos
            var fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            // Página inicial
            PDPage page = new PDPage(A4_LANDSCAPE);
            doc.addPage(page);
            int pageNo = 1;

            float pw = A4_LANDSCAPE.getWidth();
            float ph = A4_LANDSCAPE.getHeight();
            float contentW = pw - (2 * MARGIN);

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float y = ph - MARGIN;

            // Cabeçalho do documento (UserId único no topo)
            y = drawDocumentHeader(cs, fontBold, fontBody, y, all);

            // Para cada seção (categoria)
            for (String tipo : ordem) {
                List<Transaction> lista = porTipo.getOrDefault(tipo, Collections.emptyList());
                if (lista.isEmpty()) continue;

                // BRL/FX apenas se houver moeda ≠ BRL na seção
                boolean precisaFX = lista.stream()
                        .map(this::extractAmountCurrency)
                        .anyMatch(cur -> cur != null && !"BRL".equalsIgnoreCase(cur));

                // Colunas da seção
                List<Col> cols = buildSectionColumns(tipo, precisaFX);

                // Título da seção
                float needed = SECTION_TITLE_H + TABLE_HEADER_H + ROW_MIN_H;
                if (y - needed < (MARGIN + FOOTER)) {
                    cs.close();
                    page = new PDPage(A4_LANDSCAPE);
                    doc.addPage(page);
                    pageNo++;
                    cs = new PDPageContentStream(doc, page);
                    y = ph - MARGIN;
                }
                y = drawSectionTitle(cs, fontBold, y, tipo, contentW);

                // Cabeçalho da tabela
                drawTableHeader(cs, fontBold, FONT_SIZE, y, contentW, cols);
                y -= TABLE_HEADER_H;

                // Linhas
                float[] colW = absWidths(contentW, cols);
                int rowIndex = 0; // zebra por seção
                for (Transaction t : lista) {
                    float rowH = Math.max(ROW_MIN_H, LINE_SPACING); // 1 linha (ELLIPSIS)

                    // Quebra de página
                    if (y - rowH < (MARGIN + FOOTER)) {
                        cs.close();
                        page = new PDPage(A4_LANDSCAPE);
                        doc.addPage(page);
                        pageNo++;
                        cs = new PDPageContentStream(doc, page);
                        y = ph - MARGIN;

                        y = drawSectionTitle(cs, fontBold, y, tipo, contentW);
                        drawTableHeader(cs, fontBold, FONT_SIZE, y, contentW, cols);
                        y -= TABLE_HEADER_H;
                        rowIndex = 0;
                    }

                    float x = MARGIN;

                    // ===== ZEBRA =====
                    if ((rowIndex % 2) == 1) {
                        cs.setNonStrokingColor(ZEBRA_LIGHT);
                        cs.addRect(x, y - rowH, contentW, rowH);
                        cs.fill();
                        cs.setNonStrokingColor(Color.BLACK);
                    }

                    // Borda + divisores
                    cs.addRect(x, y - rowH, contentW, rowH); cs.stroke();
                    float cx = x;
                    for (float w : colW) {
                        cx += w;
                        cs.moveTo(cx, y); cs.lineTo(cx, y - rowH); cs.stroke();
                    }

                    // Conteúdo
                    float baseY = y - 4f - FONT_SIZE;
                    float txX = x + 4f;
                    for (int i = 0; i < cols.size(); i++) {
                        Col c = cols.get(i);
                        String raw = c.valueFn.apply(t);
                        String txt = ellipsize(raw, fontBody, FONT_SIZE, colW[i] - 8f, c.maxCharsHint);
                        drawAlignedText(cs, fontBody, FONT_SIZE, txX, baseY, colW[i], txt, c.align);
                        txX += colW[i];
                    }

                    y -= rowH;
                    rowIndex++;
                }

                y -= 6f; // espaço ao fim da seção
            }

            // Rodapé
            drawFooter(cs, fontBody, 8f, pageNo, MARGIN, pw);
            cs.close();

            try (var out = new ByteArrayOutputStream()) {
                doc.save(out);
                return out.toByteArray();
            }
        }
    }

    // =====================================================================
    // Construção de colunas por seção
    // =====================================================================
    private List<Col> buildSectionColumns(String tipo, boolean precisaFX) {
        List<Col> cols = new ArrayList<>();

        // Obrigatórias (todas as seções)
        cols.add(new Col("ID",               0.12f, Align.LEFT,   BreakMode.ELLIPSIS, 30, t -> safe(t.getId())));
        cols.add(new Col("Tipo",             0.06f, Align.CENTER, BreakMode.ELLIPSIS, 10, t -> safe(enumOrString(t.getType()))));
        cols.add(new Col("Valor",            0.08f, Align.RIGHT,  BreakMode.ELLIPSIS, 14, t -> formatAmountPlain(extractAmountValue(t))));
        cols.add(new Col("Moeda",            0.05f, Align.CENTER, BreakMode.ELLIPSIS,  6, t -> safe(extractAmountCurrency(t))));
        cols.add(new Col("Status",           0.08f, Align.CENTER, BreakMode.ELLIPSIS, 14, t -> safe(enumOrString(t.getStatus()))));
        cols.add(new Col("Criada em",        0.10f, Align.CENTER, BreakMode.ELLIPSIS, 19, t -> formatDateAny(t.getCreatedAt())));

        // Específicas por seção
        switch (tipo) {
            case TIPO_TRANSFERENCIA -> {
                cols.add(new Col("Target Account", 0.14f, Align.LEFT,  BreakMode.ELLIPSIS, 24, this::extractTargetAccountAny));
                cols.add(new Col("TRANSFERENCIA",  0.08f, Align.LEFT,  BreakMode.ELLIPSIS, 14, t -> safe(t.getTransferType())));
                cols.add(new Col("REGISTRO",       0.06f, Align.LEFT,  BreakMode.ELLIPSIS, 10, t -> recordFlag(t.getRecord())));
            }
            case TIPO_COMPRA -> {
                cols.add(new Col("Categoria",      0.10f, Align.LEFT,  BreakMode.ELLIPSIS, 14, t -> safe(t.getCategory())));
                cols.add(new Col("TIPO DE COMPRA", 0.08f, Align.LEFT,  BreakMode.ELLIPSIS, 14, t -> safe(t.getBuyType())));
                cols.add(new Col("REGISTRO",       0.06f, Align.LEFT,  BreakMode.ELLIPSIS, 10, t -> recordFlag(t.getRecord())));
            }
            case TIPO_DEPOSITO, TIPO_SAQUE -> {
                cols.add(new Col("REGISTRO",       0.06f, Align.LEFT,  BreakMode.ELLIPSIS, 10, t -> recordFlag(t.getRecord())));
            }
            default -> { /* mantém apenas obrigatórias */ }
        }

        // BRL/CAMBIO somente se houver moeda ≠ BRL
        if (precisaFX) {
            cols.add(new Col("BRL",     0.07f, Align.RIGHT, BreakMode.ELLIPSIS, 12, t -> formatTwoDecimals(t.getBrl())));
            cols.add(new Col("CAMBIO",  0.07f, Align.RIGHT, BreakMode.ELLIPSIS, 12, t -> formatTwoDecimals(t.getFxRate())));
        }

        normalizeRel(cols);
        return cols;
    }

    private void normalizeRel(List<Col> cols) {
        float sum = 0f;
        for (Col c : cols) sum += c.rel;
        if (Math.abs(1f - sum) < 1e-6) return;
        for (int i = 0; i < cols.size(); i++) {
            Col c = cols.get(i);
            cols.set(i, new Col(c.header, c.rel / sum, c.align, c.mode, c.maxCharsHint, c.valueFn));
        }
    }

    private float[] absWidths(float contentW, List<Col> cols) {
        float[] w = new float[cols.size()];
        for (int i = 0; i < cols.size(); i++) w[i] = contentW * cols.get(i).rel;
        return w;
    }

    // =====================================================================
    // Desenho: cabeçalho documento / seção / tabela / rodapé
    // =====================================================================
    private float drawDocumentHeader(PDPageContentStream cs,
                                     org.apache.pdfbox.pdmodel.font.PDFont fTitle,
                                     org.apache.pdfbox.pdmodel.font.PDFont fBody,
                                     float y, List<Transaction> txs) throws IOException {
        float x = MARGIN;

        cs.beginText();
        cs.setFont(fTitle, FONT_SIZE_TITLE);
        cs.newLineAtOffset(x, y);
        cs.showText("Extrato de Transações — Agrupado por categoria");
        cs.endText();
        y -= 16f;

        cs.beginText();
        cs.setFont(fBody, 9f);
        cs.newLineAtOffset(x, y);
        cs.showText("Emissão: " + dtFmt.format(LocalDateTime.now()));
        cs.endText();
        y -= 12f;

        String user = (txs != null && !txs.isEmpty()) ? safe(txs.get(0).getUserId()) : "-";
        cs.beginText();
        cs.setFont(fBody, 9f);
        cs.newLineAtOffset(x, y);
        cs.showText("Usuário: " + user);
        cs.endText();

        return y - (HEADER_SPACING + 2f);
    }

    private float drawSectionTitle(PDPageContentStream cs,
                                   org.apache.pdfbox.pdmodel.font.PDFont fBold,
                                   float y, String tipo, float contentW) throws IOException {
        float x = MARGIN;
        cs.beginText();
        cs.setFont(fBold, 10f);
        cs.newLineAtOffset(x, y - (SECTION_TITLE_H - 10f));
        cs.showText(sectionLabel(tipo));
        cs.endText();

        cs.moveTo(x, y - SECTION_TITLE_H);
        cs.lineTo(x + contentW, y - SECTION_TITLE_H);
        cs.stroke();
        return y - SECTION_TITLE_H;
    }

    private String sectionLabel(String tipo) {
        return switch (tipo) {
            case TIPO_DEPOSITO      -> "DEPÓSITOS";
            case TIPO_SAQUE         -> "SAQUES";
            case TIPO_TRANSFERENCIA -> "TRANSFERÊNCIAS";
            case TIPO_COMPRA        -> "COMPRAS";
            default                 -> safe(tipo).toUpperCase(Locale.ROOT);
        };
    }

    private void drawTableHeader(PDPageContentStream cs,
                                 org.apache.pdfbox.pdmodel.font.PDFont fBold,
                                 float fontSize,
                                 float y, float contentW, List<Col> cols) throws IOException {
        float x = MARGIN, h = TABLE_HEADER_H;
        cs.addRect(x, y - h, contentW, h); cs.stroke();

        float[] colW = absWidths(contentW, cols);
        float cx = x;
        float textY = y - (h - (fontSize + 2f)) / 2 - fontSize;

        for (int i = 0; i < cols.size(); i++) {
            cs.moveTo(cx + colW[i], y);
            cs.lineTo(cx + colW[i], y - h);
            cs.stroke();

            cs.beginText();
            cs.setFont(fBold, fontSize);
            float startX = cx + 4f;
            cs.newLineAtOffset(startX, textY);
            cs.showText(cols.get(i).header);
            cs.endText();

            cx += colW[i];
        }
    }

    private void drawFooter(PDPageContentStream cs,
                            org.apache.pdfbox.pdmodel.font.PDFont font,
                            float size, int pageNo, float margin, float pw) throws IOException {
        String text = "Página " + pageNo;
        float w = font.getStringWidth(text) / 1000f * size;
        float x = pw - margin - w;
        float y = margin - 12f;
        cs.beginText(); cs.setFont(font, size); cs.newLineAtOffset(x, y); cs.showText(text); cs.endText();
    }

    private void drawAlignedText(PDPageContentStream cs,
                                 org.apache.pdfbox.pdmodel.font.PDFont font, float size,
                                 float cellX, float baseY, float cellW,
                                 String text, Align align) throws IOException {
        if (text == null) text = "-";
        float txtW = font.getStringWidth(text) / 1000f * size;
        float x = switch (align) {
            case LEFT   -> cellX;
            case CENTER -> cellX + Math.max(0, (cellW - 8f - txtW) / 2f);
            case RIGHT  -> cellX + Math.max(0, (cellW - 8f - txtW));
        };
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, baseY);
        cs.showText(text);
        cs.endText();
    }

    // =====================================================================
    // Helpers de dados / formatação
    // =====================================================================
    private String ellipsize(String text, org.apache.pdfbox.pdmodel.font.PDFont font,
                             float size, float maxWidth, int maxCharsHint) throws IOException {
        if (text == null || text.isBlank()) return "-";
        String s = text.strip();
        if (maxCharsHint > 0 && s.length() > maxCharsHint) s = s.substring(0, maxCharsHint);
        if (font.getStringWidth(s) / 1000f * size <= maxWidth) return s;

        String ellipsis = "…";
        while (s.length() > 1 && (font.getStringWidth(s + ellipsis) / 1000f * size) > maxWidth) {
            s = s.substring(0, s.length() - 1);
        }
        return s + ellipsis;
    }

    private String enumOrString(Object o) {
        return (o instanceof Enum<?> e) ? e.name() : (o == null ? "-" : o.toString());
    }

    private String safe(Object o) {
        return (o == null || o.toString().isBlank()) ? "-" : o.toString();
    }

    private String recordFlag(Object record) {
        if (record == null) return "F";
        if (record instanceof Boolean b) return b ? "V" : "F";
        String s = record.toString().trim();
        if (s.equalsIgnoreCase("true") || s.equals("1")) return "V";
        return "F";
    }

    private String formatDateAny(Object dt) {
        if (dt == null) return "-";
        try {
            if (dt instanceof LocalDateTime ldt) return dtFmt.format(ldt);
            if (dt instanceof OffsetDateTime odt) return dtFmt.format(odt.toLocalDateTime());
            return dtFmt.format(LocalDateTime.parse(dt.toString()));
        } catch (Exception e) {
            return dt.toString();
        }
    }

    private BigDecimal extractAmountValue(Transaction t) {
        if (t == null || t.getAmount() == null) return null;
        try { return t.getAmount().value(); } catch (Throwable ignored) {
            try { return (BigDecimal) t.getAmount().getClass().getMethod("value").invoke(t.getAmount()); }
            catch (Throwable ignore2) { return null; }
        }
    }

    private String extractAmountCurrency(Transaction t) {
        if (t == null || t.getAmount() == null) return null;
        try { Object c = t.getAmount().currency(); return c==null?null:c.toString(); } catch (Throwable ignored) {
            try { Object c = t.getAmount().getClass().getMethod("currency").invoke(t.getAmount()); return c==null?null:c.toString(); }
            catch (Throwable ignore2) { return null; }
        }
    }

    // Target Account: tenta ID direto no Transaction e, se não houver, ID dentro do objeto targetAccount
    private String extractTargetAccountAny(Transaction t) {
        if (t == null) return "-";

        // IDs diretos (record / POJO)
        try { Object v = t.getClass().getMethod("targetAccountId").invoke(t);
            if (v != null && !v.toString().isBlank()) return v.toString(); } catch (Throwable ignored) { }
        try { Object v = t.getClass().getMethod("getTargetAccountId").invoke(t);
            if (v != null && !v.toString().isBlank()) return v.toString(); } catch (Throwable ignored) { }

        // Objeto targetAccount
        Object ta = null;
        try { ta = t.getClass().getMethod("targetAccount").invoke(t); } catch (Throwable ignored) {
            try { ta = t.getClass().getMethod("getTargetAccount").invoke(t); } catch (Throwable ignored2) { }
        }
        if (ta != null) {
            try { Object v = ta.getClass().getMethod("getAccountId").invoke(ta);
                if (v != null && !v.toString().isBlank()) return v.toString(); } catch (Throwable ignored) { }
            try { Object v = ta.getClass().getMethod("accountId").invoke(ta);
                if (v != null && !v.toString().isBlank()) return v.toString(); } catch (Throwable ignored) { }
            try { Object v = ta.getClass().getMethod("getId").invoke(ta);
                if (v != null && !v.toString().isBlank()) return v.toString(); } catch (Throwable ignored) { }
            try { Object v = ta.getClass().getMethod("id").invoke(ta);
                if (v != null && !v.toString().isBlank()) return v.toString(); } catch (Throwable ignored) { }

            String s = ta.toString();
            if (s != null && !s.isBlank() && !"null".equalsIgnoreCase(s)) return s;
        }
        return "-";
    }

    private String formatAmountPlain(BigDecimal v) {
        return v==null ? "0" : v.stripTrailingZeros().toPlainString();
    }

    // 2 casas decimais para BRL e CAMBIO; quando nulo/vazio → "-"
    private String formatTwoDecimals(Object v) {
        if (v == null) return "0";
        String raw = v.toString().trim();
        if (raw.isEmpty() || raw.equals("0") || raw.equalsIgnoreCase("null")) return "0";
        try {
            if (v instanceof BigDecimal bd) return twoDec.format(bd);
            if (v instanceof Number n)      return twoDec.format(n.doubleValue());
            String normalized = raw.replace(" ", "").replace(".", "").replace(",", ".");
            BigDecimal bd = new BigDecimal(normalized);
            return twoDec.format(bd);
        } catch (Exception e) {
            return "0";
        }
    }

    private org.apache.pdfbox.pdmodel.font.PDFont tryLoadUnicodeFont(PDDocument doc, String classpathTtf) {
        try (InputStream in = getClass().getResourceAsStream(classpathTtf)) {
            if (in != null) return PDType0Font.load(doc, in);
        } catch (IOException ignored) {}
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    }
}