package com.example.isp_backend.repository;

import com.example.isp_backend.entity.ChecklistTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChecklistTemplateRepository extends JpaRepository<ChecklistTemplate, Long> {
}