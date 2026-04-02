package com.survey.site.repository;

import com.survey.site.model.ChecklistQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChecklistQuestionRepository extends JpaRepository<ChecklistQuestion, Long> {
}