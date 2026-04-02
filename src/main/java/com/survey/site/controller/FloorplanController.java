package com.survey.site.controller;

import com.survey.site.model.Floor;
import com.survey.site.model.Floorplan;
import com.survey.site.repository.FloorRepository;
import com.survey.site.repository.FloorplanRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/floorplans")
@CrossOrigin("*")
public class FloorplanController {

    @Autowired
    private FloorplanRepository floorplanRepository;

    @Autowired
    private FloorRepository floorRepository;

    // ✅ GET ALL FLOORPLANS (IMPORTANT FIX)
    @GetMapping
    public List<Floorplan> getAll() {
        return floorplanRepository.findAll();
    }

    // ✅ UPLOAD
    @PostMapping("/upload/{floorId}")
    public ResponseEntity<?> upload(
            @PathVariable Long floorId,
            @RequestParam("file") MultipartFile file) {

        try {
            Floor floor = floorRepository.findById(floorId)
                    .orElseThrow(() -> new RuntimeException("Floor not found"));

            Floorplan fp = new Floorplan();
            fp.setName(file.getOriginalFilename());
            fp.setContentType(file.getContentType());
            fp.setData(file.getBytes());
            fp.setFloor(floor);

            floorplanRepository.save(fp);

            return ResponseEntity.ok("Uploaded");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload failed");
        }
    }

    // ✅ VIEW
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> get(@PathVariable Long id) {

        Floorplan fp = floorplanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        return ResponseEntity.ok()
                .header("Content-Type", fp.getContentType())
                .body(fp.getData());
    }
}