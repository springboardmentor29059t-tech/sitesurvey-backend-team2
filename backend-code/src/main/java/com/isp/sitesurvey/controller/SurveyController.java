package com.isp.sitesurvey.controller;

import com.isp.sitesurvey.entity.SurveyResponse;
import com.isp.sitesurvey.entity.Space;
import com.isp.sitesurvey.repository.SurveyResponseRepository;
import com.isp.sitesurvey.repository.SpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
public class SurveyController {

    private final SurveyResponseRepository surveyResponseRepository;
    private final SpaceRepository spaceRepository;
    private final com.isp.sitesurvey.service.AuditLogService auditLogService;

    @GetMapping("/check")
    public ResponseEntity<?> getSurveyBySpace(@RequestParam("spaceId") Long spaceId) {
        System.out.println("Checking survey status via RequestParam for space ID: " + spaceId);
        return surveyResponseRepository.findBySpaceId(spaceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/space/{spaceId}", consumes = {"multipart/form-data"})
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ONSITE_ENGINEER')")
    public ResponseEntity<?> saveSurvey(
            @PathVariable("spaceId") Long spaceId, 
            @RequestPart("surveyData") SurveyResponse surveyData,
            @RequestParam(value = "photo", required = false) org.springframework.web.multipart.MultipartFile photo,
            @RequestParam(value = "status", defaultValue = "SUBMITTED") String status) {
        try {
            Space space = spaceRepository.findById(spaceId)
                    .orElseThrow(() -> new RuntimeException("Space not found"));
            
            Optional<SurveyResponse> existing = surveyResponseRepository.findBySpaceId(spaceId);
            SurveyResponse toSave = existing.orElse(new SurveyResponse());
            
            toSave.setSpace(space);
            toSave.setPowerAvailable(surveyData.getPowerAvailable());
            toSave.setSignalStrengthDbm(surveyData.getSignalStrengthDbm());
            toSave.setCoolingAvailable(surveyData.getCoolingAvailable());
            toSave.setObstacles(surveyData.getObstacles());
            toSave.setAdditionalNotes(surveyData.getAdditionalNotes());
            toSave.setSurveyorId(surveyData.getSurveyorId());
            toSave.setStatus(status);

            if (photo != null && !photo.isEmpty()) {
                toSave.setPhotoData(photo.getBytes());
                toSave.setPhotoContentType(photo.getContentType());
            }

            // Clear old responses if any (re-survey scenario)
            toSave.getChecklistResponses().clear();
            if (surveyData.getChecklistResponses() != null) {
                surveyData.getChecklistResponses().forEach(cr -> {
                    cr.setSurveyResponse(toSave);
                    toSave.getChecklistResponses().add(cr);
                });
            }
            
            SurveyResponse saved = surveyResponseRepository.save(toSave);
            if (!"DRAFT".equalsIgnoreCase(status)) {
                auditLogService.log("SAVE_SURVEY", "Space", spaceId, "Survey completed for space: " + space.getName());
            }
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/debug")
    public ResponseEntity<?> debugListAll() {
        return ResponseEntity.ok(surveyResponseRepository.findAll());
    }
}
