package com.isp.sitesurvey.controller;

import com.isp.sitesurvey.entity.ChecklistItem;
import com.isp.sitesurvey.entity.ChecklistTemplate;
import com.isp.sitesurvey.entity.SurveyResponse;
import com.isp.sitesurvey.entity.SurveyResponseItem;
import com.isp.sitesurvey.repository.ChecklistTemplateRepository;
import com.isp.sitesurvey.repository.SurveyResponseRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/checklists")
@RequiredArgsConstructor
@CrossOrigin(
    origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"},
    methods = {
        org.springframework.web.bind.annotation.RequestMethod.GET,
        org.springframework.web.bind.annotation.RequestMethod.POST,
        org.springframework.web.bind.annotation.RequestMethod.PUT,
        org.springframework.web.bind.annotation.RequestMethod.PATCH,
        org.springframework.web.bind.annotation.RequestMethod.DELETE,
        org.springframework.web.bind.annotation.RequestMethod.OPTIONS
    }
)
public class ChecklistController {

    private final ChecklistTemplateRepository templateRepository;

    @Autowired
    private SurveyResponseRepository responseRepository;

    @Autowired
    private EntityManager entityManager;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /* ─── TEMPLATES ──────────────────────────────────────────────────── */

    @GetMapping
    public ResponseEntity<List<ChecklistTemplate>> getAllTemplates() {
        return ResponseEntity.ok(templateRepository.findAll());
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ChecklistTemplate> getTemplate(@PathVariable Long id) {
        return templateRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ChecklistTemplate> createTemplate(@RequestBody ChecklistTemplate template) {
        if (template.getItems() != null) {
            for (ChecklistItem item : template.getItems()) {
                item.setTemplate(template);
            }
        }
        return ResponseEntity.ok(templateRepository.save(template));
    }

    @PutMapping("/{id:\\d+}")
    @Transactional
    public ResponseEntity<ChecklistTemplate> updateTemplate(
            @PathVariable Long id,
            @RequestBody ChecklistTemplate updated) {
        return templateRepository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setDescription(updated.getDescription());
            existing.setTargetRole(updated.getTargetRole());
            existing.getItems().clear();
            entityManager.flush();
            if (updated.getItems() != null) {
                for (ChecklistItem item : updated.getItems()) {
                    item.setTemplate(existing);
                    existing.getItems().add(item);
                }
            }
            return ResponseEntity.ok(templateRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id:\\d+}")
    @Transactional
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        if (!templateRepository.existsById(id)) return ResponseEntity.notFound().build();
        templateRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /* ─── RESPONSES ──────────────────────────────────────────────────── */

    @GetMapping("/responses")
    public ResponseEntity<List<SurveyResponse>> getResponses(
            @RequestParam(value = "propertyId", required = false) String propertyIdStr,
            @RequestParam(value = "engineerId", required = false) String engineerIdStr,
            @RequestParam(value = "status", required = false) String status) {

        // Safely parse parameters to avoid Spring Boot 400 Bad Request errors
        Long propertyId = null;
        Long engineerId = null;

        try {
            if (propertyIdStr != null && !propertyIdStr.isBlank() && !propertyIdStr.equals("undefined") && !propertyIdStr.equals("null")) {
                propertyId = Long.parseLong(propertyIdStr);
            }
            if (engineerIdStr != null && !engineerIdStr.isBlank() && !engineerIdStr.equals("undefined") && !engineerIdStr.equals("null")) {
                engineerId = Long.parseLong(engineerIdStr);
            }
        } catch (NumberFormatException e) {
            // If the frontend sends junk data, just ignore it and fetch everything
            System.out.println("Warning: Invalid number format received in ChecklistController");
        }

        List<SurveyResponse> results;
        
        if (propertyId != null && engineerId != null) {
            results = responseRepository.findByPropertyIdAndEngineerId(propertyId, engineerId);
        } else if (propertyId != null) {
            results = responseRepository.findByPropertyId(propertyId);
        } else if (engineerId != null) {
            results = responseRepository.findByEngineerId(engineerId);
        } else {
            results = responseRepository.findAll();
        }

        if (status != null && !status.isBlank() && !status.equals("undefined") && !status.equals("null")) {
            try {
                SurveyResponse.Status s = SurveyResponse.Status.valueOf(status.toUpperCase());
                results = results.stream().filter(r -> r.getStatus() == s).toList();
            } catch (IllegalArgumentException ignored) {}
        }
        
        return ResponseEntity.ok(results);
    }

    @GetMapping("/responses/{id:\\d+}")
    public ResponseEntity<SurveyResponse> getResponse(@PathVariable Long id) {
        return responseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** POST — create a new DRAFT response on first save */
    @PostMapping("/responses")
    @Transactional
    public ResponseEntity<SurveyResponse> createResponse(@RequestBody SurveyResponse response) {
        linkAnswers(response);
        if (response.getStatus() == null) {
            response.setStatus(SurveyResponse.Status.DRAFT);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(responseRepository.save(response));
    }

    /**
     * PUT /api/checklists/responses/{id}  — autosave draft
     */
    @PutMapping("/responses/{id:\\d+}")
    @Transactional
    public ResponseEntity<SurveyResponse> updateResponse(
            @PathVariable Long id,
            @RequestBody SurveyResponse updated) {

        return responseRepository.findById(id).map(existing -> {
            // Already submitted — return as-is (200 not 409) so frontend stops autosaving
            if (existing.getStatus() == SurveyResponse.Status.SUBMITTED) {
                return ResponseEntity.ok(existing);
            }
            // Flush orphan deletes before re-inserting to prevent constraint violation
            existing.getAnswers().clear();
            entityManager.flush();

            if (updated.getAnswers() != null) {
                for (SurveyResponseItem item : updated.getAnswers()) {
                    item.setSurveyResponse(existing);
                    existing.getAnswers().add(item);
                }
            }
            if (updated.getStatus() != null) {
                existing.setStatus(updated.getStatus());
            }
            return ResponseEntity.ok(responseRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * PATCH /api/checklists/responses/{id}/submit
     */
    @PatchMapping("/responses/{id:\\d+}/submit")
    @Transactional
    public ResponseEntity<?> finaliseResponse(@PathVariable Long id) {
        Optional<SurveyResponse> opt = responseRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        SurveyResponse response = opt.get();

        // Idempotent — already submitted is fine
        if (response.getStatus() == SurveyResponse.Status.SUBMITTED) {
            return ResponseEntity.ok(response);
        }

        // Validate required fields only if a template is linked
        Long templateId = response.getChecklistTemplateId();
        if (templateId != null) {
            Optional<ChecklistTemplate> tplOpt = templateRepository.findById(templateId);
            if (tplOpt.isPresent()) {
                // Collect ALL missing required fields first, then return them together
                List<String> missing = new ArrayList<>();
                for (ChecklistItem item : tplOpt.get().getItems()) {
                    if (!Boolean.TRUE.equals(item.getIsRequired())) continue;
                    boolean answered = response.getAnswers().stream()
                            .anyMatch(a -> a.getChecklistItemId().equals(item.getId())
                                    && a.getAnswerText() != null
                                    && !a.getAnswerText().isBlank());
                    if (!answered) {
                        String label = (item.getQuestion() != null && !item.getQuestion().isBlank())
                                ? item.getQuestion() : "Item #" + item.getId();
                        missing.add(label);
                    }
                }
                if (!missing.isEmpty()) {
                    // 422 Unprocessable Entity — structured so frontend can display field names
                    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                            .body(Map.of(
                                "error",   "Required fields not answered",
                                "missing", missing
                            ));
                }
            }
        }

        response.setStatus(SurveyResponse.Status.SUBMITTED);
        return ResponseEntity.ok(responseRepository.save(response));
    }

    /* ─── ATTACHMENTS ────────────────────────────────────────────────── */

    @PostMapping("/responses/{responseId:\\d+}/items/{itemId:\\d+}/photo")
    @Transactional
    public ResponseEntity<?> uploadPhoto(
            @PathVariable Long responseId,
            @PathVariable Long itemId,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only image files are accepted"));
        }

        Optional<SurveyResponse> opt = responseRepository.findById(responseId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        SurveyResponse response = opt.get();

        if (response.getStatus() == SurveyResponse.Status.SUBMITTED) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Cannot attach files to a submitted response"));
        }

        SurveyResponseItem target = response.getAnswers().stream()
                .filter(a -> a.getChecklistItemId().equals(itemId))
                .findFirst()
                .orElseGet(() -> {
                    SurveyResponseItem n = new SurveyResponseItem();
                    n.setChecklistItemId(itemId);
                    n.setSurveyResponse(response);
                    response.getAnswers().add(n);
                    return n;
                });

        try {
            String ext        = getExtension(file.getOriginalFilename());
            String stored     = "checklist_" + responseId + "_" + itemId + "_" + UUID.randomUUID() + ext;
            Path   uploadPath = Paths.get(uploadDir, "checklists");
            Files.createDirectories(uploadPath);
            Files.copy(file.getInputStream(), uploadPath.resolve(stored),
                    StandardCopyOption.REPLACE_EXISTING);

            String url = "/api/files/checklists/" + stored;
            target.setPhotoUrl(url);
            target.setPhotoFilename(file.getOriginalFilename());
            responseRepository.save(response);

            return ResponseEntity.ok(Map.of(
                    "url", url, "filename", file.getOriginalFilename(),
                    "itemId", itemId, "responseId", responseId));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    /* ─── Helpers ─────────────────────────────────────────────────────── */

    private void linkAnswers(SurveyResponse response) {
        if (response.getAnswers() != null) {
            response.getAnswers().forEach(item -> item.setSurveyResponse(response));
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf('.'));
    }
}