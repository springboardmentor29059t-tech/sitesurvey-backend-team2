package com.survey.site.controller;

import com.survey.site.model.ChecklistTemplate;
import com.survey.site.service.ChecklistTemplateService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@CrossOrigin
public class ChecklistTemplateController {

    @Autowired
    private ChecklistTemplateService service;

    // ✅ CREATE TEMPLATE
    @PostMapping
    public ResponseEntity<?> createTemplate(@RequestBody ChecklistTemplate template) {
        try {
            ChecklistTemplate saved = service.createTemplate(template);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating template: " + e.getMessage());
        }
    }

    // ✅ GET ALL TEMPLATES
    @GetMapping
    public ResponseEntity<List<ChecklistTemplate>> getAllTemplates() {
        return ResponseEntity.ok(service.getAllTemplates());
    }

    // ✅ GET BY SPACE TYPE
    @GetMapping("/space/{spaceType}")
    public ResponseEntity<List<ChecklistTemplate>> getBySpaceType(@PathVariable String spaceType) {
        return ResponseEntity.ok(service.getBySpaceType(spaceType));
    }

    // ✅ GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {

        ChecklistTemplate template = service.getById(id);

        if (template == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(template);
    }

    // ✅ UPDATE TEMPLATE
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTemplate(
            @PathVariable Long id,
            @RequestBody ChecklistTemplate template) {

        ChecklistTemplate updated = service.updateTemplate(id, template);

        if (updated == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updated);
    }

    // ✅ DELETE TEMPLATE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTemplate(@PathVariable Long id) {

        boolean deleted = service.deleteTemplate(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok("Template deleted successfully");
    }
}