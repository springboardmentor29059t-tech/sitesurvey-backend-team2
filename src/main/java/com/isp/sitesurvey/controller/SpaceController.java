package com.isp.sitesurvey.controller;

import com.isp.sitesurvey.entity.Space;
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
            @SuppressWarnings("unchecked")
            List<Map<String, String>> rows = (List<Map<String, String>>) request.get("rows");
            Long actualFloorId = requestedId;

            // --- Fail-safe Floor Creation ---
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

            int count = csvImportService.importSpaces(actualFloorId, rows);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", count,
                "message", "Successfully imported " + count + " spaces"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // --- FETCH SPACES FOR SURVEY CANVAS ---
    @GetMapping("/floor/{floorId}")
    public ResponseEntity<List<Space>> getSpacesByFloor(@PathVariable Long floorId) {
        try {
            List<Space> spaces = entityManager.createQuery(
                "SELECT s FROM Space s WHERE s.floor.id = :floorId", Space.class)
                .setParameter("floorId", floorId)
                .getResultList();
            return ResponseEntity.ok(spaces);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteSpace(@PathVariable Long id) {
        try {
            int deleted = entityManager.createNativeQuery("DELETE FROM spaces WHERE id = :id")
                .setParameter("id", id)
                .executeUpdate();
            return ResponseEntity.ok(Map.of("success", deleted > 0));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @DeleteMapping("/property/{propertyId}")
    @Transactional
    public ResponseEntity<?> clearPropertySpaces(@PathVariable Long propertyId) {
        try {
            // Find all space IDs belonging to this property through the floor/building chain
            String selectSql = "SELECT s.id FROM spaces s " +
                               "JOIN floors f ON s.floor_id = f.id " +
                               "JOIN buildings b ON f.building_id = b.id " +
                               "WHERE b.property_id = :propId";
                               
            @SuppressWarnings("unchecked")
            List<Object> rawIds = entityManager.createNativeQuery(selectSql)
                                    .setParameter("propId", propertyId)
                                    .getResultList();
            
            if (!rawIds.isEmpty()) {
                List<Long> ids = rawIds.stream()
                                       .map(id -> Long.valueOf(id.toString()))
                                       .collect(Collectors.toList());
                                       
                entityManager.createNativeQuery("DELETE FROM spaces WHERE id IN (:ids)")
                             .setParameter("ids", ids)
                             .executeUpdate();
            }
            return ResponseEntity.ok(Map.of("success", true, "message", "Cleared " + rawIds.size() + " spaces"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}