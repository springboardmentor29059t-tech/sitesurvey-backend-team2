package com.isp.sitesurvey.controller;

import com.isp.sitesurvey.service.CSVImportService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/spaces")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
public class SpaceController {

    private final CSVImportService csvImportService;
    private final EntityManager entityManager;

    @PostMapping("/import/preview")
    public ResponseEntity<Map<String, Object>> previewImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("floorId") Long floorId) {
        try {
            return ResponseEntity.ok(csvImportService.previewCSV(file, floorId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/import/confirm")
    @Transactional
    public ResponseEntity<Map<String, Object>> confirmImport(@RequestBody Map<String, Object> request) {
        try {
            Long requestedId = Long.valueOf(request.get("floorId").toString());
            List<Map<String, String>> rows = (List<Map<String, String>>) request.get("rows");
            Long actualFloorId = requestedId;

            // Fail-safe Floor Creation
            try {
                Number floorExists = (Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM floors WHERE id = :id")
                    .setParameter("id", requestedId).getSingleResult();

                if (floorExists.longValue() == 0) {
                    entityManager.createNativeQuery("INSERT INTO buildings (property_id, name, floor_count) VALUES (:propId, 'Main Building', 1)")
                        .setParameter("propId", requestedId).executeUpdate();
                    Number buildingId = (Number) entityManager.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult();
                    
                    entityManager.createNativeQuery("INSERT INTO floors (building_id, level_label) VALUES (:bldgId, 'Ground Floor')")
                        .setParameter("bldgId", buildingId.longValue()).executeUpdate();
                    actualFloorId = ((Number) entityManager.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult()).longValue();
                }
            } catch (Exception ex) {
                System.out.println("Auto-creation skipped: " + ex.getMessage());
            }

            int count = csvImportService.importSpaces(actualFloorId, rows);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", count);
            response.put("message", "Successfully imported " + count + " spaces");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==========================================
    // NEW: DELETE SPECIFIC SPACE
    // ==========================================
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteSpace(@PathVariable Long id) {
        try {
            entityManager.createNativeQuery("DELETE FROM spaces WHERE id = :id")
                .setParameter("id", id)
                .executeUpdate();
            return ResponseEntity.ok(Map.of("success", true, "message", "Space deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==========================================
    // NEW: CLEAR ALL SPACES FOR A PROPERTY
    // ==========================================
    @DeleteMapping("/property/{propertyId}")
    @Transactional
    public ResponseEntity<?> clearPropertySpaces(@PathVariable Long propertyId) {
        try {
            String selectSql = "SELECT s.id FROM spaces s " +
                               "LEFT JOIN floors f ON s.floor_id = f.id " +
                               "LEFT JOIN buildings b ON f.building_id = b.id " +
                               "WHERE b.property_id = :propId OR s.floor_id = :propId OR f.id = :propId";
                               
            List<Number> spaceIds = entityManager.createNativeQuery(selectSql)
                                    .setParameter("propId", propertyId)
                                    .getResultList();
            
            if (!spaceIds.isEmpty()) {
                List<Long> ids = spaceIds.stream().map(Number::longValue).collect(Collectors.toList());
                entityManager.createNativeQuery("DELETE FROM spaces WHERE id IN (:ids)")
                             .setParameter("ids", ids)
                             .executeUpdate();
            }
            return ResponseEntity.ok(Map.of("success", true, "message", "All spaces cleared"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}