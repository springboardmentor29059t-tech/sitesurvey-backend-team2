package com.isp.sitesurvey.controller;

import com.isp.sitesurvey.entity.ChecklistQuestion;
import com.isp.sitesurvey.entity.ChecklistTemplate;
import com.isp.sitesurvey.repository.ChecklistQuestionRepository;
import com.isp.sitesurvey.repository.ChecklistTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checklists")
public class ChecklistController {

    @Autowired
    private ChecklistTemplateRepository templateRepository;

    @Autowired
    private ChecklistQuestionRepository questionRepository;

    @GetMapping("/templates")
    public List<ChecklistTemplate> getAllTemplates() {
        return templateRepository.findAll();
    }

    @PostMapping("/templates")
    public ChecklistTemplate createTemplate(@RequestBody ChecklistTemplate template) {
        if (template.getQuestions() != null) {
            template.getQuestions().forEach(q -> q.setTemplate(template));
        }
        return templateRepository.save(template);
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<?> deleteTemplate(@PathVariable("id") Long id) {
        templateRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Template deleted"));
    }

    @GetMapping("/templates/{id}/questions")
    public List<ChecklistQuestion> getQuestions(@PathVariable("id") Long id) {
        return questionRepository.findByTemplateId(id);
    }
}
