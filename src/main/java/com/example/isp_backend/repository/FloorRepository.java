package com.example.isp_backend.repository;

import com.example.isp_backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FloorRepository extends JpaRepository<Floor, Long> {

    List<Floor> findByBuilding(Building building);
}