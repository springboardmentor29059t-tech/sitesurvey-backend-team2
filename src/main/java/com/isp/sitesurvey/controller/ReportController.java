package com.isp.sitesurvey.controller;

import com.isp.sitesurvey.entity.SurveyResponse;
import com.isp.sitesurvey.repository.EquipmentRepository;
import com.isp.sitesurvey.repository.PropertyRepository;
import com.isp.sitesurvey.repository.SurveyResponseRepository;
import com.isp.sitesurvey.service.PdfReportService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ReportController
 *
 * PACKAGE: com.isp.sitesurvey.controller
 * Place at: src/main/java/com/isp/sitesurvey/controller/ReportController.java
 *
 * The startup error "ClassNotFoundException: PdfReportService" was because
 * ReportController uses @RequiredArgsConstructor (Lombok) to inject
 * PdfReportService in the constructor. If PdfReportService.java is saved to
 * the wrong directory (e.g. milestone4/backend/) instead of the service
 * package, it doesn't compile into the classpath and Spring can't find it.
 *
 * Fix: save PdfReportService.java to
 *   src/main/java/com/isp/sitesurvey/service/PdfReportService.java
 * and this file to
 *   src/main/java/com/isp/sitesurvey/controller/ReportController.java
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
public class ReportController {

    private final PdfReportService         pdfReportService;
    private final PropertyRepository       propertyRepository;
    private final EquipmentRepository      equipmentRepository;
    private final SurveyResponseRepository responseRepository;
    private final EntityManager            entityManager;

    /* ─── PDF download ──────────────────────────────────────────────── */
    @GetMapping("/property/{propertyId}/pdf")
    public ResponseEntity<byte[]> downloadReport(
            @PathVariable Long propertyId,
            @RequestParam(defaultValue = "Site Engineer") String engineer) {

        var property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found: " + propertyId));

        byte[] pdf = pdfReportService.generateSurveyReport(
                propertyId,
                property.getName(),
                property.getCity() != null ? property.getCity() : "—",
                engineer);

        String filename = "survey_" + propertyId + "_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
                + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }

    /* ─── Dashboard summary ─────────────────────────────────────────── */
    @GetMapping("/dashboard/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        List<SurveyResponse> all = safeLoadAll();

        long   submitted  = all.stream().filter(this::isSubmitted).count();
        long   drafts     = all.size() - submitted;
        double completion = all.isEmpty() ? 0
                : Math.round((submitted * 100.0 / all.size()) * 10) / 10.0;

        // Per-engineer breakdown
        Map<Long, long[]> byEng = new LinkedHashMap<>();
        all.forEach(r -> {
            if (r.getEngineerId() == null) return;
            long[] c = byEng.computeIfAbsent(r.getEngineerId(), k -> new long[]{0, 0});
            if (isSubmitted(r)) c[0]++; else c[1]++;
        });
        List<Map<String, Object>> byEngineer = byEng.entrySet().stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("engineerId", e.getKey());
            m.put("submitted",  e.getValue()[0]);
            m.put("drafts",     e.getValue()[1]);
            m.put("total",      e.getValue()[0] + e.getValue()[1]);
            return m;
        }).collect(Collectors.toList());

        // Equipment counts
        Map<String, Long> equipByType = new LinkedHashMap<>();
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = entityManager
                    .createQuery("SELECT e.type, COUNT(e) FROM Equipment e GROUP BY e.type")
                    .getResultList();
            rows.forEach(r -> equipByType.put(String.valueOf(r[0]), ((Number) r[1]).longValue()));
        } catch (Exception ignored) { }

        // Recent 10 submissions
        List<Map<String, Object>> recent = all.stream()
                .filter(this::isSubmitted)
                .sorted(Comparator.comparing(
                        r -> r.getSubmittedAt() != null ? r.getSubmittedAt() : LocalDateTime.MIN,
                        Comparator.reverseOrder()))
                .limit(10)
                .map(r -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id",          r.getId());
                    m.put("propertyId",  r.getPropertyId());
                    m.put("equipmentId", r.getEquipmentId());
                    m.put("engineerId",  r.getEngineerId());
                    m.put("status",      statusLabel(r));
                    m.put("submittedAt", r.getSubmittedAt() != null
                            ? r.getSubmittedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
                    m.put("answerCount", r.getAnswers() != null ? r.getAnswers().size() : 0);
                    return m;
                }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalSurveys",    all.size());
        result.put("submitted",       submitted);
        result.put("drafts",          drafts);
        result.put("completionRate",  completion);
        result.put("byEngineer",      byEngineer);
        result.put("equipmentByType", equipByType);
        result.put("recentActivity",  recent);
        result.put("generatedAt",     LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/dashboard/property/{propertyId}")
    public ResponseEntity<Map<String, Object>> getPropertyStats(@PathVariable Long propertyId) {
        var property = propertyRepository.findById(propertyId);
        if (property.isEmpty()) return ResponseEntity.notFound().build();

        List<SurveyResponse> responses = new ArrayList<>();
        try { responses = responseRepository.findByPropertyId(propertyId); }
        catch (Exception ignored) { }

        var  equipment = equipmentRepository.findByPropertyId(propertyId);
        long submitted = responses.stream().filter(this::isSubmitted).count();

        long spaceCount = 0;
        try {
            spaceCount = ((Number) entityManager.createQuery(
                "SELECT COUNT(s) FROM Space s JOIN s.floor f JOIN f.building b WHERE b.property.id = :pid")
                .setParameter("pid", propertyId).getSingleResult()).longValue();
        } catch (Exception ignored) { }

        Map<String, Long> typeBreakdown = equipment.stream().collect(Collectors.groupingBy(
                e -> e.getType() != null ? e.getType().toLowerCase() : "unknown",
                Collectors.counting()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("propertyId",      propertyId);
        result.put("propertyName",    property.get().getName());
        result.put("totalResponses",  responses.size());
        result.put("submitted",       submitted);
        result.put("drafts",          responses.size() - submitted);
        result.put("completionRate",  responses.isEmpty() ? 0
                : Math.round((submitted * 100.0 / responses.size()) * 10) / 10.0);
        result.put("totalEquipment",  equipment.size());
        result.put("equipmentByType", typeBreakdown);
        result.put("totalSpaces",     spaceCount);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/checklists/status")
    public ResponseEntity<Map<String, Object>> getChecklistStatus() {
        List<SurveyResponse> all = safeLoadAll();

        long submitted = all.stream().filter(this::isSubmitted).count();
        long total     = all.size();

        long propertiesWithSurveys = all.stream()
                .filter(this::isSubmitted)
                .map(SurveyResponse::getPropertyId)
                .filter(Objects::nonNull)
                .distinct().count();

        Optional<LocalDateTime> lastSub = all.stream()
                .filter(r -> r.getSubmittedAt() != null)
                .map(SurveyResponse::getSubmittedAt)
                .max(Comparator.naturalOrder());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalResponses",        total);
        result.put("submitted",             submitted);
        result.put("drafts",                total - submitted);
        result.put("completionPct",         total == 0 ? 0
                : Math.round((submitted * 100.0 / total) * 10) / 10.0);
        result.put("propertiesWithSurveys", propertiesWithSurveys);
        result.put("lastSubmission",        lastSub.map(
                dt -> dt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).orElse(null));
        return ResponseEntity.ok(result);
    }

    /* ─── Helpers ────────────────────────────────────────────────────── */

    /**
     * Tries JPA findAll() first. If the status column doesn't exist yet
     * (migration not run), falls back to native SQL reading only the safe
     * columns that existed before the migration.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<SurveyResponse> safeLoadAll() {
        try {
            return responseRepository.findAll();
        } catch (Exception jpaEx) {
            try {
                List rows = entityManager.createNativeQuery(
                    "SELECT id, property_id, equipment_id, engineer_id, submitted_at " +
                    "FROM survey_responses").getResultList();

                List<SurveyResponse> result = new ArrayList<>();
                for (Object row : rows) {
                    Object[] r = (Object[]) row;
                    SurveyResponse sr = new SurveyResponse();
                    sr.setId(r[0] != null ? ((Number) r[0]).longValue() : null);
                    sr.setPropertyId(r[1] != null ? ((Number) r[1]).longValue() : null);
                    sr.setEquipmentId(r[2] != null ? String.valueOf(r[2]) : null);
                    sr.setEngineerId(r[3] != null ? ((Number) r[3]).longValue() : null);
                    if (r[4] != null) {
                        sr.setStatus(SurveyResponse.Status.SUBMITTED);
                        if (r[4] instanceof Timestamp ts) {
                            sr.setSubmittedAt(ts.toLocalDateTime());
                        }
                    }
                    result.add(sr);
                }
                return result;
            } catch (Exception ignored) {
                return Collections.emptyList();
            }
        }
    }

    private boolean isSubmitted(SurveyResponse r) {
        try { return r.getStatus() == SurveyResponse.Status.SUBMITTED; }
        catch (Exception e) { return false; }
    }

    private String statusLabel(SurveyResponse r) {
        try { return r.getStatus() != null ? r.getStatus().name() : "DRAFT"; }
        catch (Exception e) { return "DRAFT"; }
    }
}