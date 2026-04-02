

package com.survey.site.repository;

import com.survey.site.model.Survey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
    List<Survey> findByEngineerId(Long engineerId);
    List<Survey> findBySpaceId(Long spaceId);
}