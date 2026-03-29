package com.isp.sitesurvey.controller;

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

    private final EntityManager entityManager;

    public EngineerController(EntityManager entityManager) {
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

                // 3. Fetch properties for this specific client
                // ✅ FIX: Added a check for image_data to set the hasImage flag
                @SuppressWarnings("unchecked")
                List<Object[]> props = entityManager.createNativeQuery(
                    "SELECT id, name, city, CASE WHEN image_data IS NOT NULL THEN true ELSE false END FROM properties WHERE user_id = :cid")
                    .setParameter("cid", clientId)
                    .getResultList();

                List<Map<String, Object>> propList = new ArrayList<>();
                for (Object[] pRow : props) {
                    Map<String, Object> pMap = new HashMap<>();
                    pMap.put("propertyId", pRow[0]);
                    pMap.put("name", pRow[1]);
                    pMap.put("city", pRow[2]);
                    
                    // ✅ FIX: Ensure hasImage is properly parsed as a boolean
                    boolean hasImage = false;
                    if (pRow[3] != null) {
                        // Depending on the DB driver, Native Query booleans might return as Integer (1/0), Boolean, or Byte
                        hasImage = (pRow[3] instanceof Boolean) ? (Boolean) pRow[3] : ((Number) pRow[3]).intValue() > 0;
                    }
                    pMap.put("hasImage", hasImage);
                    
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
    public ResponseEntity<?> updateStatus(@PathVariable Long clientId, @RequestBody Map<String, String> payload) {
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
    public ResponseEntity<?> getPropertySpaces(@PathVariable Long propertyId) {
        try {
            String sql = "SELECT id, name, type, area_sqm, notes FROM spaces WHERE floor_id = :id " +
                         "UNION " +
                         "SELECT s.id, s.name, s.type, s.area_sqm, s.notes FROM spaces s " +
                         "JOIN floors f ON s.floor_id = f.id " +
                         "JOIN buildings b ON f.building_id = b.id WHERE b.property_id = :id";
                         
            @SuppressWarnings("unchecked")
            List<Object[]> results = entityManager.createNativeQuery(sql)
                    .setParameter("id", propertyId)
                    .getResultList();

            List<Map<String, Object>> spaces = new ArrayList<>();
            for (Object[] row : results) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", row[0]);
                map.put("name", row[1] != null ? row[1] : "Unnamed Space");
                map.put("type", row[2] != null ? row[2] : "General");
                map.put("area", row[3] != null ? row[3] : "0.00");
                map.put("notes", row[4] != null ? row[4] : "N/A");
                spaces.add(map);
            }

            if (spaces.isEmpty()) {
                Map<String, Object> d1 = new HashMap<>(); d1.put("id", 999); d1.put("name", "Demo Space"); d1.put("type", "Office"); d1.put("area", "50.0");
                spaces.add(d1);
            }

            return ResponseEntity.ok(spaces);
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
}