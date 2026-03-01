package com.example.isp_backend.repository;

import com.example.isp_backend.entity.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChecklistItemRepository
        extends JpaRepository<ChecklistItem, Long> {
}