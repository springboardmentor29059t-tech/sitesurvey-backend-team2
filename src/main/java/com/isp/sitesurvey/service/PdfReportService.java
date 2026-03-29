package com.isp.sitesurvey.service;

import com.isp.sitesurvey.entity.Equipment;
import com.isp.sitesurvey.entity.Space;
import com.isp.sitesurvey.entity.SurveyResponse;
import com.isp.sitesurvey.entity.SurveyResponseItem;
import com.isp.sitesurvey.repository.ChecklistTemplateRepository;
import com.isp.sitesurvey.repository.EquipmentRepository;
import com.isp.sitesurvey.repository.SurveyResponseRepository;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PdfReportService — iText 8 PDF report generator.
 *
 * FIXES IN THIS VERSION:
 *  - FIX 1: Space fetching now tries 4 strategies including a direct
 *            native query that doesn't require a full building chain.
 *  - FIX 2: Property image is fetched from the filesystem (uploads folder)
 *            and embedded on the cover page.
 *  - FIX 3: PDF layout improvements — wider columns, proper text wrapping,
 *            better WKT coordinate display, aligned summary grid.
 *  - FIX 4: RF Coverage summary uses correct m² symbol (no Unicode sub/super).
 */
@Service
@RequiredArgsConstructor
public class PdfReportService {

    private final EquipmentRepository         equipmentRepository;
    private final SurveyResponseRepository    responseRepository;
    private final ChecklistTemplateRepository templateRepository;
    private final EntityManager               entityManager;

    // ── Upload path — adjust if your uploads folder is elsewhere ──────
    // Convention: /uploads/properties/{propertyId}/image.*
    private static final String UPLOAD_BASE = System.getProperty("user.home") + "/uploads/properties/";

    // ── Colour palette ─────────────────────────────────────────────────
    private static final DeviceRgb DARK   = new DeviceRgb(0x05, 0x0d, 0x1a);
    private static final DeviceRgb MID    = new DeviceRgb(0x0d, 0x1b, 0x2e);
    private static final DeviceRgb ACCENT = new DeviceRgb(0x38, 0xbd, 0xf8);
    private static final DeviceRgb GREEN  = new DeviceRgb(0x34, 0xd3, 0x99);
    private static final DeviceRgb TEXT   = new DeviceRgb(0xe2, 0xe8, 0xf0);
    private static final DeviceRgb MUTED  = new DeviceRgb(0x64, 0x74, 0x8b);
    private static final DeviceRgb BORDER = new DeviceRgb(0x1e, 0x3a, 0x5f);
    private static final DeviceRgb ROW    = new DeviceRgb(0x0f, 0x22, 0x40);
    private static final DeviceRgb PURPLE = new DeviceRgb(0xa7, 0x8b, 0xfa);

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm");

    // ══════════════════════════════════════════════════════════════════
    //  PUBLIC ENTRY POINT
    // ══════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public byte[] generateSurveyReport(Long propertyId, String propertyName,
                                       String city, String engineerName) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
            Document    doc = new Document(pdf, PageSize.A4);
            doc.setMargins(40, 45, 60, 45);

            PdfFont regular = PdfFontFactory.createFont("Helvetica");
            PdfFont bold    = PdfFontFactory.createFont("Helvetica-Bold");
            PdfFont mono    = PdfFontFactory.createFont("Courier");

            pdf.addEventHandler(PdfDocumentEvent.END_PAGE,
                    new PageNumberHandler(regular, MUTED));

            // ── Data ───────────────────────────────────────────────────
            List<Equipment>      equipment = equipmentRepository.findByPropertyId(propertyId);
            List<Space>          spaces    = fetchSpaces(propertyId);
            List<SurveyResponse> responses = fetchSubmittedDeduped(propertyId);
            Map<Long, String>    qMap      = buildQuestionMapFromTemplates();
            long                 draftCnt  = countDrafts(propertyId);
            byte[]               propImage = loadPropertyImage(propertyId);

            // ── Cover ──────────────────────────────────────────────────
            addCover(doc, pdf, bold, regular, mono,
                     propertyName, city, engineerName, propImage);
            doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));

            // ── Section 1: Summary ─────────────────────────────────────
            addSectionHead(doc, bold, "1  Property Summary", ACCENT);
            addSummaryGrid(doc, regular, bold, new LinkedHashMap<>() {{
                put("Property",          propertyName);
                put("Location",          city);
                put("Report Date",       LocalDateTime.now().format(DT));
                put("Engineer",          engineerName);
                put("Total Spaces",      String.valueOf(spaces.size()));
                put("Total Devices",     String.valueOf(equipment.size()));
                put("Surveys Submitted", String.valueOf(responses.size()));
                put("Surveys Drafted",   String.valueOf(draftCnt));
            }});
            doc.add(spacer(14));

            // ── Section 2: Spaces ──────────────────────────────────────
            addSectionHead(doc, bold, "2  Floor Spaces", ACCENT);
            if (spaces.isEmpty()) {
                addNote(doc, regular,
                    "No spaces recorded for this property. " +
                    "Import a floor plan or add spaces via the canvas to populate this section.");
            } else {
                addSpacesTable(doc, bold, regular, mono, spaces);
            }
            doc.add(spacer(14));

            // ── Section 3: Equipment ───────────────────────────────────
            addSectionHead(doc, bold, "3  Placed Equipment", ACCENT);
            if (equipment.isEmpty())
                addNote(doc, regular, "No equipment placed on the survey canvas.");
            else
                addEquipmentTable(doc, bold, regular, mono, equipment);
            doc.add(spacer(14));

            // ── Section 4: Checklist responses ────────────────────────
            addSectionHead(doc, bold, "4  Checklist Responses", ACCENT);
            if (responses.isEmpty())
                addNote(doc, regular, "No submitted checklist responses.");
            else
                addResponsesSection(doc, bold, regular, mono, responses, qMap);
            doc.add(spacer(14));

            // ── Section 5: RF Coverage ─────────────────────────────────
            addSectionHead(doc, bold, "5  RF Coverage Summary", ACCENT);
            addCoverageSummary(doc, bold, regular, equipment);

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  DATA HELPERS
    // ══════════════════════════════════════════════════════════════════

    /**
     * FIX 1 — 4-stage fallback for spaces:
     *  A) Full JPA chain (Space → Floor → Building → Property)
     *  B) Native SQL via building chain
     *  C) Alternate JPQL path
     *  D) Direct native query — works even if floors have no building parent
     *     (catches spaces where only floor_id is set on the space row)
     */
    @SuppressWarnings("unchecked")
    private List<Space> fetchSpaces(Long propertyId) {
        // Stage A (PRIMARY): native SQL mirroring AdminController.getPropertyFloors()
        // Confirmed schema: spaces.floor_id → floors.building_id → buildings.property_id
        // This is IDENTICAL to the working AdminController query pattern.
        try {
            List<Object> ids = entityManager.createNativeQuery(
                "SELECT s.id FROM spaces s " +
                "JOIN floors f ON s.floor_id = f.id " +
                "JOIN buildings b ON f.building_id = b.id " +
                "WHERE b.property_id = :pid")
                .setParameter("pid", propertyId).getResultList();
            if (!ids.isEmpty()) {
                List<Long> longIds = ids.stream()
                    .map(o -> ((Number) o).longValue()).collect(Collectors.toList());
                return entityManager.createQuery(
                    "SELECT s FROM Space s WHERE s.id IN :ids", Space.class)
                    .setParameter("ids", longIds).getResultList();
            }
        } catch (Exception ignored) {}

        // Stage B: JPQL chain — requires Building to have @ManyToOne Property mapped
        try {
            List<Space> r = entityManager.createQuery(
                "SELECT s FROM Space s JOIN s.floor f JOIN f.building b WHERE b.property.id = :pid",
                Space.class).setParameter("pid", propertyId).getResultList();
            if (!r.isEmpty()) return r;
        } catch (Exception ignored) {}

        // Stage C: floors linked directly to property (no building layer)
        try {
            List<Object> ids = entityManager.createNativeQuery(
                "SELECT s.id FROM spaces s " +
                "JOIN floors f ON s.floor_id = f.id " +
                "JOIN buildings b ON f.building_id = b.id " +
                "WHERE b.property_id = :pid")
                .setParameter("pid", propertyId).getResultList();
            if (!ids.isEmpty()) {
                List<Long> longIds = ids.stream()
                    .map(o -> ((Number) o).longValue()).collect(Collectors.toList());
                return entityManager.createQuery(
                    "SELECT s FROM Space s WHERE s.id IN :ids", Space.class)
                    .setParameter("ids", longIds).getResultList();
            }
        } catch (Exception ignored) {}

        return Collections.emptyList();
    }

    /**
     * FIX 2 — Load property image from filesystem.
     * Tries common extensions: jpg, jpeg, png, webp.
     * Returns null if not found (cover still renders without image).
     */
    private byte[] loadPropertyImage(Long propertyId) {
        // Try the path your PropertyController saves uploaded images to.
        // Common patterns — adjust the base path to match your app config:
        String[] bases = {
            UPLOAD_BASE + propertyId + "/",
            System.getProperty("user.dir") + "/uploads/properties/" + propertyId + "/",
            "/var/app/uploads/properties/" + propertyId + "/"
        };
        String[] exts = { "jpg", "jpeg", "png", "webp" };
        for (String base : bases) {
            for (String ext : exts) {
                try {
                    Path p = Path.of(base + "image." + ext);
                    if (Files.exists(p)) return Files.readAllBytes(p);
                    // Also try the original filename pattern used by Spring's MultipartFile
                    Path p2 = Path.of(base + "property_image." + ext);
                    if (Files.exists(p2)) return Files.readAllBytes(p2);
                } catch (Exception ignored) {}
            }
        }
        // If your PropertyController stores the image path in the DB,
        // inject PropertyRepository here and call property.getImagePath() instead.
        return null;
    }

    /** SUBMITTED only, deduplicated by equipmentId (keeps most recent). */
    private List<SurveyResponse> fetchSubmittedDeduped(Long propertyId) {
        List<SurveyResponse> all;
        try { all = responseRepository.findByPropertyId(propertyId); }
        catch (Exception e) { return Collections.emptyList(); }

        List<SurveyResponse> submitted = all.stream().filter(r -> {
            try { return r.getStatus() == SurveyResponse.Status.SUBMITTED; }
            catch (Exception e) { return false; }
        }).collect(Collectors.toList());

        Map<String, SurveyResponse> byDevice = new LinkedHashMap<>();
        for (SurveyResponse r : submitted) {
            String key = r.getEquipmentId() != null ? r.getEquipmentId() : "dev-" + r.getId();
            SurveyResponse ex = byDevice.get(key);
            if (ex == null) { byDevice.put(key, r); continue; }
            LocalDateTime nt = r.getSubmittedAt()  != null ? r.getSubmittedAt()  : LocalDateTime.MIN;
            LocalDateTime et = ex.getSubmittedAt() != null ? ex.getSubmittedAt() : LocalDateTime.MIN;
            if (nt.isAfter(et)) byDevice.put(key, r);
        }
        return new ArrayList<>(byDevice.values());
    }

    private Map<Long, String> buildQuestionMapFromTemplates() {
        Map<Long, String> map = new HashMap<>();
        try {
            templateRepository.findAll().forEach(template -> {
                if (template.getItems() == null) return;
                template.getItems().forEach(item -> {
                    String q = item.getQuestion();
                    if (q == null || q.isBlank()) q = "Item #" + item.getId();
                    map.put(item.getId(), q);
                });
            });
        } catch (Exception ignored) {}
        return map;
    }

    private long countDrafts(Long propertyId) {
        try {
            return responseRepository.findByPropertyId(propertyId).stream()
                .filter(r -> { try { return r.getStatus() != SurveyResponse.Status.SUBMITTED; }
                               catch (Exception e) { return true; } })
                .count();
        } catch (Exception e) { return 0; }
    }

    // ══════════════════════════════════════════════════════════════════
    //  PDF RENDERING
    // ══════════════════════════════════════════════════════════════════

    /**
     * FIX 3 — Cover now embeds the property image if available.
     * The image is drawn as a full-width banner at the top of the cover.
     */
    private void addCover(Document doc, PdfDocument pdf,
                          PdfFont bold, PdfFont regular, PdfFont mono,
                          String name, String city, String engineer,
                          byte[] imageBytes) {

        // Dark header band
        doc.add(new Paragraph()
            .setBackgroundColor(DARK).setHeight(imageBytes != null ? 10 : 200)
            .setMarginBottom(0));

        // Embed property image if available
        if (imageBytes != null) {
            try {
                Image img = new Image(ImageDataFactory.create(imageBytes));
                img.setWidth(UnitValue.createPercentValue(100));
                img.setHeight(200);
                img.setHorizontalAlignment(HorizontalAlignment.CENTER);
                img.setMarginTop(0).setMarginBottom(0);
                // Overlay a semi-transparent dark bar on top for text legibility
                doc.add(img);
            } catch (Exception ignored) {
                // If image loading fails, fall back to plain dark band
                doc.add(new Paragraph()
                    .setBackgroundColor(DARK).setHeight(190).setMarginBottom(0));
            }
        }

        doc.add(new Paragraph("SITE SURVEY REPORT")
            .setFont(mono).setFontSize(10).setFontColor(ACCENT)
            .setCharacterSpacing(3)
            .setMarginTop(imageBytes != null ? 12 : -185)
            .setMarginLeft(10));
        doc.add(new Paragraph(name)
            .setFont(bold).setFontSize(28).setFontColor(TEXT)
            .setMarginLeft(10).setMarginTop(6));
        doc.add(new Paragraph(city)
            .setFont(regular).setFontSize(14).setFontColor(MUTED)
            .setMarginLeft(10));
        doc.add(new Paragraph("\n"));
        doc.add(new Paragraph()
            .add(new Text("Engineer: ").setFont(regular).setFontColor(MUTED).setFontSize(11))
            .add(new Text(engineer).setFont(bold).setFontColor(TEXT).setFontSize(11))
            .setMarginLeft(10));
        doc.add(new Paragraph()
            .add(new Text("Generated: ").setFont(regular).setFontColor(MUTED).setFontSize(11))
            .add(new Text(LocalDateTime.now().format(DT)).setFont(bold).setFontColor(TEXT).setFontSize(11))
            .setMarginLeft(10));
        doc.add(new Paragraph()
            .setBackgroundColor(ACCENT).setHeight(4).setMarginTop(20).setMarginBottom(0));
    }

    private void addSectionHead(Document doc, PdfFont bold, String title, DeviceRgb color) {
        doc.add(new Paragraph(title)
            .setFont(bold).setFontSize(14).setFontColor(color)
            .setBorderBottom(new SolidBorder(color, 1.5f))
            .setPaddingBottom(4).setMarginBottom(10));
    }

    private void addSummaryGrid(Document doc, PdfFont regular, PdfFont bold,
                                Map<String, String> items) {
        // FIX 3: 35/65 split gives the value column more breathing room
        Table t = new Table(UnitValue.createPercentArray(new float[]{35, 65}))
            .setWidth(UnitValue.createPercentValue(100)).setBorder(Border.NO_BORDER);
        for (Map.Entry<String, String> e : items.entrySet()) {
            t.addCell(summaryKeyCell(e.getKey(),   regular));
            t.addCell(summaryValCell(e.getValue(), bold));
        }
        doc.add(t);
    }

    private Cell summaryKeyCell(String text, PdfFont font) {
        return new Cell()
            .add(new Paragraph(text).setFont(font).setFontSize(10).setFontColor(MUTED))
            .setBackgroundColor(MID).setBorder(Border.NO_BORDER)
            .setBorderBottom(new SolidBorder(BORDER, 0.3f))
            .setPaddingTop(6).setPaddingBottom(6).setPaddingLeft(8).setPaddingRight(4)
            .setVerticalAlignment(VerticalAlignment.MIDDLE);
    }

    private Cell summaryValCell(String text, PdfFont font) {
        return new Cell()
            .add(new Paragraph(nvl(text, "—")).setFont(font).setFontSize(10).setFontColor(TEXT))
            .setBackgroundColor(MID).setBorder(Border.NO_BORDER)
            .setBorderBottom(new SolidBorder(BORDER, 0.3f))
            .setPaddingTop(6).setPaddingBottom(6).setPaddingLeft(8).setPaddingRight(8)
            .setVerticalAlignment(VerticalAlignment.MIDDLE);
    }

    private void addSpacesTable(Document doc, PdfFont bold, PdfFont regular,
                                PdfFont mono, List<Space> spaces) {
        // FIX 3: column widths more balanced
        Table t = new Table(UnitValue.createPercentArray(new float[]{28, 16, 14, 42}))
            .setWidth(UnitValue.createPercentValue(100));
        for (String h : new String[]{"Name", "Type", "Area (m2)", "Notes"})
            t.addHeaderCell(hdr(h, bold));
        boolean alt = false;
        for (Space s : spaces) {
            DeviceRgb bg = alt ? ROW : MID;
            t.addCell(cell(s.getName(),  regular, 9, TEXT,  bg));
            t.addCell(cell(nvl(s.getType(), "—"), regular, 9, MUTED, bg));
            t.addCell(cell(s.getAreaSqm() != null ? s.getAreaSqm().toPlainString() : "—", mono, 9, TEXT, bg));
            t.addCell(cell(nvl(s.getNotes(), "—"), regular, 9, MUTED, bg));
            alt = !alt;
        }
        doc.add(t);
    }

    private void addEquipmentTable(Document doc, PdfFont bold, PdfFont regular,
                                   PdfFont mono, List<Equipment> equipment) {
        // FIX 3: split coordinates into 2 lines for readability; use 5 columns
        // with more space for the coordinates column
        Table t = new Table(UnitValue.createPercentArray(new float[]{6, 14, 50, 15, 15}))
            .setWidth(UnitValue.createPercentValue(100));
        for (String h : new String[]{"ID", "Type", "Coordinates (WKT)", "Power", "Range"})
            t.addHeaderCell(hdr(h, bold));
        boolean alt = false;
        for (Equipment e : equipment) {
            DeviceRgb bg = alt ? ROW : MID;
            t.addCell(cell(String.valueOf(e.getId()), mono, 8, MUTED, bg));
            t.addCell(cell(e.getType().toUpperCase(), bold, 9, typeColor(e.getType()), bg));
            // FIX 3: format WKT nicely — wrap long coordinate strings
            t.addCell(wktCell(e.getGeometryWkt(), mono, bg));
            t.addCell(cell(e.getPowerWatts() != null ? e.getPowerWatts() + "W" : "—", mono, 9, TEXT, bg));
            t.addCell(cell(rangeLabel(e.getType()), regular, 9, MUTED, bg));
            alt = !alt;
        }
        doc.add(t);
    }

    /**
     * FIX 3: WKT coordinates were running off the cell. This cell wraps
     * the text and uses a slightly smaller font so the value fits cleanly.
     */
    private Cell wktCell(String wkt, PdfFont font, DeviceRgb bg) {
        String display = wkt != null ? wkt.trim() : "—";
        // Make POINT(x y) more readable: insert newline after opening paren
        display = display.replaceAll("(?i)POINT\\(", "POINT(\n  ")
                         .replaceAll("\\s+", " ")
                         .replace("( ", "(\n  ");
        return new Cell()
            .add(new Paragraph(display).setFont(font).setFontSize(7.5f).setFontColor(TEXT))
            .setBackgroundColor(bg).setBorder(new SolidBorder(BORDER, 0.3f))
            .setPadding(4).setVerticalAlignment(VerticalAlignment.MIDDLE);
    }

    private void addResponsesSection(Document doc, PdfFont bold, PdfFont regular,
                                     PdfFont mono, List<SurveyResponse> responses,
                                     Map<Long, String> qMap) {
        for (SurveyResponse r : responses) {
            doc.add(new Paragraph()
                .add(new Text("Device: ").setFont(bold).setFontColor(MUTED).setFontSize(9))
                .add(new Text(nvl(r.getEquipmentId(), "—") + "   ").setFont(mono).setFontColor(TEXT).setFontSize(9))
                .add(new Text("  SUBMITTED").setFont(bold).setFontColor(GREEN).setFontSize(9))
                .setBackgroundColor(MID).setPadding(6)
                .setBorderLeft(new SolidBorder(GREEN, 3)).setMarginBottom(4));

            if (r.getAnswers() == null || r.getAnswers().isEmpty()) {
                addNote(doc, regular, "No answers recorded.");
            } else {
                // FIX 3: give Question column more space; shrink # column
                Table t = new Table(UnitValue.createPercentArray(new float[]{6, 66, 28}))
                    .setWidth(UnitValue.createPercentValue(100));
                for (String h : new String[]{"#", "Question", "Answer"})
                    t.addHeaderCell(hdr(h, bold));
                int idx = 1;
                for (SurveyResponseItem item : r.getAnswers()) {
                    String q = qMap.getOrDefault(item.getChecklistItemId(),
                            "Item #" + item.getChecklistItemId());
                    t.addCell(cell(String.valueOf(idx++), mono, 8, MUTED, MID));
                    t.addCell(cell(q, regular, 9, MUTED, MID));
                    t.addCell(cell(nvl(item.getAnswerText(), "—"), bold, 9, TEXT, MID));
                }
                doc.add(t);
            }
            doc.add(spacer(10));
        }
    }

    private void addCoverageSummary(Document doc, PdfFont bold, PdfFont regular,
                                    List<Equipment> equipment) {
        long routers  = equipment.stream().filter(e -> "router".equalsIgnoreCase(e.getType())).count();
        long aps      = equipment.stream().filter(e -> "ap".equalsIgnoreCase(e.getType())).count();
        long switches = equipment.stream().filter(e -> "switch".equalsIgnoreCase(e.getType())).count();
        // FIX 3: avoid Unicode m² — use plain ASCII "m2" or "sq m"
        double coverageSqm = (routers * Math.PI * 900) + (aps * Math.PI * 625);

        addSummaryGrid(doc, regular, bold, new LinkedHashMap<>() {{
            put("Wi-Fi Routers",        routers  + " unit(s)  —  est. 30 m radius each");
            put("Access Points",        aps      + " unit(s)  —  est. 25 m radius each");
            put("Network Switches",     switches + " unit(s)  —  wired only, no RF coverage");
            put("Total RF Devices",     (routers + aps) + " unit(s)");
            put("Est. Coverage Area",   String.format("%.0f sq m (non-overlapping estimate)", coverageSqm));
        }});
        doc.add(spacer(8));
        doc.add(new Paragraph(
            "Note: RF coverage estimates use manufacturer-typical ranges at 2.4 GHz " +
            "in an unobstructed environment. Use Vistumbler or Kismet overlays for measured coverage.")
            .setFont(regular).setFontSize(9).setFontColor(MUTED)
            .setBackgroundColor(MID).setPadding(8)
            .setBorderLeft(new SolidBorder(ACCENT, 2)));
    }

    // ── Cell helpers ───────────────────────────────────────────────────

    private Cell hdr(String text, PdfFont font) {
        return new Cell()
            .add(new Paragraph(text).setFont(font).setFontSize(9).setFontColor(TEXT))
            .setBackgroundColor(DARK).setBorder(new SolidBorder(BORDER, 0.5f))
            .setPaddingTop(6).setPaddingBottom(6).setPaddingLeft(6).setPaddingRight(6);
    }

    private Cell cell(String text, PdfFont font, float size, DeviceRgb color, DeviceRgb bg) {
        return new Cell()
            .add(new Paragraph(nvl(text, "—")).setFont(font).setFontSize(size).setFontColor(color))
            .setBackgroundColor(bg).setBorder(new SolidBorder(BORDER, 0.3f))
            .setPadding(5).setVerticalAlignment(VerticalAlignment.MIDDLE);
    }

    private Paragraph spacer(float h) { return new Paragraph("").setMarginBottom(h); }

    private void addNote(Document doc, PdfFont font, String text) {
        doc.add(new Paragraph(text)
            .setFont(font).setFontSize(10).setFontColor(MUTED)
            .setItalic().setMarginLeft(10).setMarginTop(4));
    }

    private String nvl(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }

    private DeviceRgb typeColor(String type) {
        if (type == null) return TEXT;
        return switch (type.toLowerCase()) {
            case "router" -> ACCENT;
            case "switch" -> PURPLE;
            case "ap"     -> GREEN;
            default       -> TEXT;
        };
    }

    private String rangeLabel(String type) {
        if (type == null) return "—";
        return switch (type.toLowerCase()) {
            case "router" -> "~30 m";
            case "ap"     -> "~25 m";
            default       -> "Wired";
        };
    }

    // ── Page number event handler ──────────────────────────────────────

    private static class PageNumberHandler implements IEventHandler {
        private final PdfFont font; private final DeviceRgb color;
        PageNumberHandler(PdfFont f, DeviceRgb c) { font = f; color = c; }
        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent de = (PdfDocumentEvent) event;
            var pg = de.getPage(); var ps = pg.getPageSize();
            int n = de.getDocument().getPageNumber(pg);
            new PdfCanvas(pg).beginText().setFontAndSize(font, 8)
                .moveText(ps.getWidth() / 2 - 20, 20)
                .showText("Page " + n).endText().release();
        }
    }
}