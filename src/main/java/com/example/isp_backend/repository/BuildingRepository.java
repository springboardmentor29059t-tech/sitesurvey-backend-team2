package com.example.isp_backend.repository;

import com.example.isp_backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BuildingRepository extends JpaRepository<Building, Long> {

    List<Building> findByProperty(Property property);
}