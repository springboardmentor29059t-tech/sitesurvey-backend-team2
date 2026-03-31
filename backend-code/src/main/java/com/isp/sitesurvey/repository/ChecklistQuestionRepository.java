package com.isp.sitesurvey.repository;

import com.isp.sitesurvey.entity.ChecklistQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChecklistQuestionRepository extends JpaRepository<ChecklistQuestion, Long> {
    List<ChecklistQuestion> findByTemplateId(Long templateId);
}
