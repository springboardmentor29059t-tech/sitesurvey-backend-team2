package com.isp.sitesurvey.repository;

import com.isp.sitesurvey.entity.SurveyResponse;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {

    // This ensures the dashboard can fetch EVERYTHING including answers in one go
    @Override
    @EntityGraph(attributePaths = {"answers"})
    List<SurveyResponse> findAll();

    @EntityGraph(attributePaths = {"answers"})
    List<SurveyResponse> findByPropertyId(Long propertyId);

    @EntityGraph(attributePaths = {"answers"})
    List<SurveyResponse> findByEngineerId(Long engineerId);

    @EntityGraph(attributePaths = {"answers"})
    List<SurveyResponse> findByPropertyIdAndEngineerId(Long propertyId, Long engineerId);

    @EntityGraph(attributePaths = {"answers"})
    List<SurveyResponse> findByEquipmentId(String equipmentId);

    @EntityGraph(attributePaths = {"answers"})
    List<SurveyResponse> findByStatus(SurveyResponse.Status status);
}