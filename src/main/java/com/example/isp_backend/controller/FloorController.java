package com.example.isp_backend.controller;

import com.example.isp_backend.entity.*;
import com.example.isp_backend.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/floors")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class FloorController {

    private final FloorRepository floorRepository;
    private final BuildingRepository buildingRepository;

    // ===============================
    // Create Floor under Building
    // ===============================
    @PostMapping("/{buildingId}")
    public Floor createFloor(
            @PathVariable Long buildingId,
            @RequestBody Floor floor
    ) {

        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new RuntimeException("Building not found"));

        floor.setBuilding(building);
        floor.setCreatedAt(LocalDateTime.now());

        return floorRepository.save(floor);
    }

    // ===============================
    // Get Floors by Building
    // ===============================
    @GetMapping("/building/{buildingId}")
    public List<Floor> getFloorsByBuilding(@PathVariable Long buildingId) {

        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new RuntimeException("Building not found"));

        return floorRepository.findByBuilding(building);
    }

    // ===============================
    // Upload Floor Plan
    // ===============================
    @PostMapping("/{floorId}/upload")
    public Floor uploadFloorPlan(
            @PathVariable Long floorId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> new RuntimeException("Floor not found"));

        String uploadDir = System.getProperty("user.dir")
                + File.separator + "uploads" + File.separator;

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File destination = new File(uploadDir + fileName);

        file.transferTo(destination);

        floor.setFloorPlanPath("uploads/" + fileName);

        return floorRepository.save(floor);
    }

    // ===============================
    // Delete Floor
    // ===============================
    @DeleteMapping("/{id}")
    public String deleteFloor(@PathVariable Long id) {

        Floor floor = floorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Floor not found"));

        floorRepository.delete(floor);

        return "Floor deleted successfully";
    }
}
