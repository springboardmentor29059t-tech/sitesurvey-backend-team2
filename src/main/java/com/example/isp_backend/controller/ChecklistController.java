package com.example.isp_backend.controller;

import com.example.isp_backend.entity.*;
import com.example.isp_backend.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/checklists")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ChecklistController {

    private final ChecklistTemplateRepository templateRepository;
    private final SurveyChecklistRepository surveyChecklistRepository;
    private final FloorRepository floorRepository;
    private final SpaceRepository spaceRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final ChecklistItemRepository checklistItemRepository;

    // ===============================
    // Get all templates
    // ===============================
    @GetMapping("/templates")
    public List<ChecklistTemplate> getTemplates() {
        return templateRepository.findAll();
    }

    // ===============================
    // Create checklist for Floor
    // ===============================
    @PostMapping("/floor/{floorId}")
    public SurveyChecklist createForFloor(@PathVariable Long floorId) {

        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> new RuntimeException("Floor not found"));

        SurveyChecklist checklist = new SurveyChecklist();
        checklist.setFloor(floor);
        checklist.setStatus("DRAFT");
        checklist.setCreatedAt(LocalDateTime.now());

        return surveyChecklistRepository.save(checklist);
    }

    // ===============================
    // Create checklist for Space
    // ===============================
    @PostMapping("/space/{spaceId}")
    public SurveyChecklist createForSpace(@PathVariable Long spaceId) {

        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new RuntimeException("Space not found"));

        SurveyChecklist checklist = new SurveyChecklist();
        checklist.setSpace(space);
        checklist.setStatus("DRAFT");
        checklist.setCreatedAt(LocalDateTime.now());

        return surveyChecklistRepository.save(checklist);
    }

    // ===============================
    // Save Response
    // ===============================
    @PostMapping("/response")
    public SurveyResponse saveResponse(@RequestBody SurveyResponse response) {

        ChecklistItem item = checklistItemRepository.findById(
                        response.getChecklistItem().getId())
                .orElseThrow(() -> new RuntimeException("Item not found"));

        SurveyChecklist checklist = surveyChecklistRepository.findById(
                        response.getSurveyChecklist().getId())
                .orElseThrow(() -> new RuntimeException("Checklist not found"));

        response.setChecklistItem(item);
        response.setSurveyChecklist(checklist);

        return surveyResponseRepository.save(response);
    }

    // ===============================
    // Submit Checklist
    // ===============================
    @PutMapping("/submit/{id}")
    public SurveyChecklist submitChecklist(@PathVariable Long id) {

        SurveyChecklist checklist = surveyChecklistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Checklist not found"));

        checklist.setStatus("SUBMITTED");

        return surveyChecklistRepository.save(checklist);
    }

    // ===============================
    // Get checklist by floor
    // ===============================
    @GetMapping("/floor/{floorId}")
    public SurveyChecklist getByFloor(@PathVariable Long floorId) {

        return surveyChecklistRepository
                .findByFloorId(floorId)
                .orElse(null);
    }

    // ===============================
    // Get checklist by space
    // ===============================
    @GetMapping("/space/{spaceId}")
    public SurveyChecklist getBySpace(@PathVariable Long spaceId) {

        return surveyChecklistRepository
                .findBySpaceId(spaceId)
                .orElse(null);
    }
}
