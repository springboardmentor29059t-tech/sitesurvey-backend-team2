package com.example.isp_backend.controller;

import com.example.isp_backend.entity.*;
import com.example.isp_backend.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/spaces")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class SpaceController {

    private final SpaceRepository spaceRepository;
    private final FloorRepository floorRepository;

    // ============================
    // Get Spaces by Floor
    // ============================
    @GetMapping("/floor/{floorId}")
    public List<Space> getSpacesByFloor(@PathVariable Long floorId) {

        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> new RuntimeException("Floor not found"));

        return spaceRepository.findByFloor(floor);
    }

    // ============================
    // Bulk Upload Spaces CSV
    // ============================
    @PostMapping("/upload/{floorId}")
    public String uploadSpaces(
            @PathVariable Long floorId,
            @RequestParam("file") MultipartFile file
    ) {

        try {

            Floor floor = floorRepository.findById(floorId)
                    .orElseThrow(() -> new RuntimeException("Floor not found"));

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream())
            );

            reader.readLine(); // skip header

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                String[] data = line.split(",");

                Space space = new Space();
                space.setName(data[0].trim());
                space.setType(data[1].trim());
                space.setArea(Double.parseDouble(data[2].trim()));
                space.setCreatedAt(LocalDateTime.now());
                space.setFloor(floor);

                spaceRepository.save(space);
            }

            return "Spaces uploaded successfully";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error uploading spaces";
        }
    }

    // ============================
    // Delete Space
    // ============================
    @DeleteMapping("/{id}")
    public String deleteSpace(@PathVariable Long id) {

        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Space not found"));

        spaceRepository.delete(space);

        return "Space deleted successfully";
    }
}
