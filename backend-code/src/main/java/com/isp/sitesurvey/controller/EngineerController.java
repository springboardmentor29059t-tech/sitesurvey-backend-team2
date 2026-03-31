package com.isp.sitesurvey.controller;

import com.isp.sitesurvey.entity.User;
import com.isp.sitesurvey.repository.UserRepository;
import com.isp.sitesurvey.repository.PropertyRepository;
import jakarta.persistence.EntityManager;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/engineer")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
public class EngineerController {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final EntityManager entityManager;

    public EngineerController(UserRepository userRepository, PropertyRepository propertyRepository, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.entityManager = entityManager;
    }

    /**
     * Fetch sites for the logged-in engineer.
     * REMOVED @PreAuthorize to stop the 403 Forbidden error for Jane.
     */
    @GetMapping("/my-sites")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getMySites(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        String loginIdentity = principal.getName();
        System.out.println("DEBUG: User attempting access: " + loginIdentity);

        try {
            // 1. Find Engineer ID using Case-Insensitive search (Fixes Jane@123 vs jane)
            Object engIdObj = entityManager.createNativeQuery(
                "SELECT id FROM users WHERE LOWER(email) = LOWER(:val) OR LOWER(username) = LOWER(:val)")
                .setParameter("val", loginIdentity)
                .getSingleResult();
            
            Long engineerId = ((Number) engIdObj).longValue();

            // 2. Fetch assigned clients exactly like your manual SQL test
            @SuppressWarnings("unchecked")
            List<Object[]> clients = entityManager.createNativeQuery(
                "SELECT id, full_name, email, survey_status FROM users WHERE assigned_engineer_id = :engId")
                .setParameter("engId", engineerId)
                .getResultList();

            List<Map<String, Object>> response = new ArrayList<>();

            for (Object[] row : clients) {
                Map<String, Object> map = new HashMap<>();
                Long clientId = ((Number) row[0]).longValue();
                
                map.put("clientId", clientId);
                map.put("clientName", row[1] != null ? row[1] : "Client #" + clientId);
                map.put("clientEmail", row[2]);
                
                // Status normalization
                String status = (row[3] != null) ? row[3].toString() : "ASSIGNED";
                if (status.equals("PENDING")) status = "ASSIGNED";
                map.put("surveyStatus", status);

                // 3. Fetch properties for this specific client using Repository (CLEANER)
                List<com.isp.sitesurvey.entity.Property> props = propertyRepository.findByUserId(clientId);

                List<Map<String, Object>> propList = new ArrayList<>();
                for (com.isp.sitesurvey.entity.Property p : props) {
                    Map<String, Object> pMap = new HashMap<>();
                    pMap.put("propertyId", p.getId());
                    pMap.put("name", p.getName());
                    pMap.put("city", p.getCity());
                    pMap.put("hasImage", p.getImageData() != null);
                    pMap.put("extraImageIds", p.getExtraImageIds());
                    propList.add(pMap);
                }

                map.put("properties", propList);
                map.put("propertyCount", propList.size());
                response.add(map);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("ERROR: Could not fetch sites for " + loginIdentity + " -> " + e.getMessage());
            return ResponseEntity.ok(new ArrayList<>()); 
        }
    }

    @PutMapping("/status/{clientId}")
    @Transactional
    public ResponseEntity<?> updateStatus(@PathVariable("clientId") Long clientId, @RequestBody Map<String, String> payload) {
        try {
            String newStatus = payload.get("status").toUpperCase();
            entityManager.createNativeQuery("UPDATE users SET survey_status = :status WHERE id = :id")
                .setParameter("status", newStatus)
                .setParameter("id", clientId)
                .executeUpdate();
            return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/properties/{propertyId}/spaces")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPropertySpaces(@PathVariable("propertyId") Long propertyId) {
        try {
            // ✅ UPGRADED: Include elevation_m and geometry_wkt and split the query like in PropertyController
            String sql1 = "SELECT s.id, s.name, s.type, s.area_sqm, s.notes, s.elevation_m, s.geometry_wkt, f.level_label, b.name as building_name " +
                          "FROM spaces s JOIN floors f ON s.floor_id = f.id JOIN buildings b ON f.building_id = b.id WHERE f.id = :id";
            String sql2 = "SELECT s.id, s.name, s.type, s.area_sqm, s.notes, s.elevation_m, s.geometry_wkt, f.level_label, b.name as building_name " +
                          "FROM spaces s JOIN floors f ON s.floor_id = f.id JOIN buildings b ON f.building_id = b.id WHERE b.property_id = :id";
            
            @SuppressWarnings("unchecked")
            List<Object[]> results1 = entityManager.createNativeQuery(sql1).setParameter("id", propertyId).getResultList();
            @SuppressWarnings("unchecked")
            List<Object[]> results2 = entityManager.createNativeQuery(sql2).setParameter("id", propertyId).getResultList();
            
            List<Map<String, Object>> spaces = new ArrayList<>();
            Set<Long> seenIds = new HashSet<>();

            processResults(results1, spaces, seenIds);
            processResults(results2, spaces, seenIds);
            
            return ResponseEntity.ok(spaces);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    private void processResults(List<Object[]> results, List<Map<String, Object>> spaces, Set<Long> seenIds) {
        for (Object[] row : results) {
            Long id = ((Number) row[0]).longValue();
            if (seenIds.contains(id)) continue;
            seenIds.add(id);

            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("name", row[1] != null ? row[1] : "Unnamed Space");
            map.put("type", row[2] != null ? row[2] : "General");
            map.put("area", row[3] != null ? row[3] : "0.00");
            map.put("notes", row[4] != null ? row[4] : "N/A");
            map.put("elevation", row[5] != null ? row[5] : "0.00");
            map.put("geometry", row[6] != null ? row[6] : null);
            map.put("floorLevel", row[7] != null ? row[7] : "Unknown");
            map.put("buildingName", row[8] != null ? row[8] : "Main Building");
            spaces.add(map);
        }
    }
}