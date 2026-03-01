
package com.example.isp_backend.controller;

import com.example.isp_backend.entity.*;
import com.example.isp_backend.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class BuildingController {

    private final BuildingRepository buildingRepository;
    private final PropertyRepository propertyRepository;

    // ===============================
    // Create Building under Property
    // ===============================
    @PostMapping("/{propertyId}")
    public Building createBuilding(
            @PathVariable Long propertyId,
            @RequestBody Building building
    ) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        building.setProperty(property);
        building.setCreatedAt(LocalDateTime.now());

        return buildingRepository.save(building);
    }

    // ===============================
    // Get Buildings by Property
    // ===============================
    @GetMapping("/property/{propertyId}")
    public List<Building> getBuildingsByProperty(@PathVariable Long propertyId) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        return buildingRepository.findByProperty(property);
    }

    // ===============================
    // Delete Building
    // ===============================
    @DeleteMapping("/{id}")
    public String deleteBuilding(@PathVariable Long id) {

        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Building not found"));

        buildingRepository.delete(building);

        return "Building deleted successfully";
    }
}