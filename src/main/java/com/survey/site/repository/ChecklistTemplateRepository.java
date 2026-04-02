package com.survey.site.repository;

import com.survey.site.model.ChecklistTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChecklistTemplateRepository extends JpaRepository<ChecklistTemplate, Long> {

    List<ChecklistTemplate> findBySpaceType(String spaceType);
}