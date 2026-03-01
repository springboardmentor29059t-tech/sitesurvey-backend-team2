package com.example.isp_backend.repository;

import com.example.isp_backend.entity.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyResponseRepository
        extends JpaRepository<SurveyResponse, Long> {
}
