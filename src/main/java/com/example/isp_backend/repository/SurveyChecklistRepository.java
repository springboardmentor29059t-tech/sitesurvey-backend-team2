package com.example.isp_backend.repository;

import com.example.isp_backend.entity.SurveyChecklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SurveyChecklistRepository
        extends JpaRepository<SurveyChecklist, Long> {

    Optional<SurveyChecklist> findByFloorId(Long floorId);

    Optional<SurveyChecklist> findBySpaceId(Long spaceId);
}