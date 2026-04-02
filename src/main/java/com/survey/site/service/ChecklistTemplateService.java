package com.survey.site.service;

import com.survey.site.model.ChecklistTemplate;
import com.survey.site.repository.ChecklistTemplateRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChecklistTemplateService {

    @Autowired
    private ChecklistTemplateRepository repository;


    public ChecklistTemplate createTemplate(ChecklistTemplate template) {

        // 🔥 IMPORTANT: set template reference for questions
        if (template.getQuestions() != null) {
            template.getQuestions().forEach(q -> q.setTemplate(template));
        }

        return repository.save(template);
    }


    public List<ChecklistTemplate> getAllTemplates() {
        return repository.findAll();
    }


    public List<ChecklistTemplate> getBySpaceType(String spaceType) {
        return repository.findBySpaceType(spaceType);
    }


    public ChecklistTemplate getById(Long id) {
        Optional<ChecklistTemplate> optional = repository.findById(id);
        return optional.orElse(null);
    }


    public ChecklistTemplate updateTemplate(Long id, ChecklistTemplate updatedTemplate) {

        Optional<ChecklistTemplate> optional = repository.findById(id);

        if (optional.isPresent()) {
            ChecklistTemplate existing = optional.get();

            existing.setName(updatedTemplate.getName());
            existing.setSpaceType(updatedTemplate.getSpaceType());

            // 🔥 replace questions completely
            existing.getQuestions().clear();

            if (updatedTemplate.getQuestions() != null) {
                updatedTemplate.getQuestions().forEach(q -> {
                    q.setTemplate(existing);
                    existing.getQuestions().add(q);
                });
            }

            return repository.save(existing);
        }

        return null;
    }


    public boolean deleteTemplate(Long id) {

        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }

        return false;
    }
}