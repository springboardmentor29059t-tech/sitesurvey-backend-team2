package com.isp.sitesurvey.controller;

import com.isp.sitesurvey.entity.User;
import com.isp.sitesurvey.entity.Property;
import com.isp.sitesurvey.repository.UserRepository;
import com.isp.sitesurvey.repository.PropertyRepository;
import jakarta.persistence.EntityManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/account-manager")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
@PreAuthorize("hasRole('ACCOUNT_MANAGER')")
public class AccountManagerController {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final EntityManager entityManager;

    public AccountManagerController(UserRepository userRepository, 
                                    PropertyRepository propertyRepository,
                                    EntityManager entityManager) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.entityManager = entityManager;
    }

    /**
     * Get all data for workload assignment view
     * Returns: Engineers with their current load + All clients with assignment status
     */
    @GetMapping("/workload-data")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getWorkloadData() {
        List<User> allUsers = userRepository.findAll();

        // FIX: Used .contains("ENGINEER") to catch both "ONSITE_ENGINEER" and "ROLE_ONSITE_ENGINEER"
        List<Map<String, Object>> engineers = allUsers.stream()
            .filter(u -> u.getRoles().stream()
                .anyMatch(r -> r.getName().contains("ENGINEER")))
            .map(engineer -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", engineer.getId());
                map.put("fullName", engineer.getFullName() != null ? engineer.getFullName() : engineer.getUsername());
                map.put("email", engineer.getEmail());
                
                // Count assigned clients for this engineer
                long assignedCount = allUsers.stream()
                    .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().contains("CLIENT")))
                    .filter(u -> u.getAssignedEngineer() != null && u.getAssignedEngineer().getId().equals(engineer.getId()))
                    .count();
                
                map.put("currentLoad", assignedCount);
                
                // Count completed work for this engineer (FIXED: Now reads from DB status)
                long completedCount = allUsers.stream()
                    .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().contains("CLIENT")))
                    .filter(u -> u.getAssignedEngineer() != null && u.getAssignedEngineer().getId().equals(engineer.getId()))
                    .filter(u -> "COMPLETED".equals(u.getSurveyStatus()))
                    .count();
                
                map.put("completedSurveys", completedCount);
                
                return map;
            })
            .collect(Collectors.toList());

        // FIX: Used .contains("CLIENT") to catch both "CLIENT" and "ROLE_CLIENT"
        List<Map<String, Object>> clients = allUsers.stream()
            .filter(u -> u.getRoles().stream()
                .anyMatch(r -> r.getName().contains("CLIENT")))
            .map(client -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", client.getId());
                map.put("fullName", client.getFullName() != null ? client.getFullName() : client.getUsername());
                map.put("email", client.getEmail());
                
                // Get assigned engineer info
                if (client.getAssignedEngineer() != null) {
                    map.put("engineerName", client.getAssignedEngineer().getFullName());
                    map.put("engineerId", client.getAssignedEngineer().getId());
                } else {
                    map.put("engineerName", "Unassigned");
                    map.put("engineerId", null);
                }
                
                // Calculate survey status based on properties
                String status = calculateClientStatus(client);
                map.put("surveyStatus", status);
                
                // Count properties for this client
                long propertyCount = propertyRepository.findAll().stream()
                    .filter(p -> p.getUser() != null && p.getUser().getId().equals(client.getId()))
                    .count();
                map.put("propertyCount", propertyCount);
                
                return map;
            })
            .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("engineers", engineers);
        response.put("clients", clients);
        response.put("totalClients", clients.size());
        response.put("totalEngineers", engineers.size());
        response.put("unassignedClients", clients.stream()
            .filter(c -> "Unassigned".equals(c.get("engineerName")))
            .count());

        return ResponseEntity.ok(response);
    }

    /**
     * Assign multiple clients to an engineer
     */
    @PostMapping("/assign-bulk")
    @Transactional
    public ResponseEntity<?> assignBulk(@RequestBody Map<String, Object> payload) {
        try {
            Long engineerId = Long.valueOf(payload.get("engineerId").toString());
            List<Long> clientIds = ((List<?>) payload.get("clientIds")).stream()
                    .map(id -> Long.valueOf(id.toString()))
                    .collect(Collectors.toList());

            // Validate engineer exists and has correct role
            User engineer = userRepository.findById(engineerId)
                    .orElseThrow(() -> new RuntimeException("Engineer not found"));
            
            // FIX: Using .contains("ENGINEER")
            boolean isEngineer = engineer.getRoles().stream()
                .anyMatch(r -> r.getName().contains("ENGINEER"));
            
            if (!isEngineer) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Selected user is not an Onsite Engineer"));
            }

            // Assign clients to engineer
            List<User> clients = userRepository.findAllById(clientIds);
            clients.forEach(client -> {
                client.setAssignedEngineer(engineer);
                
                // FIX: Update status instantly to ASSIGNED so Jane sees it properly
                if (client.getSurveyStatus() == null || client.getSurveyStatus().equals("PENDING") || client.getSurveyStatus().equals("NO_PROPERTY")) {
                    client.setSurveyStatus("ASSIGNED");
                }
            });
            
            userRepository.saveAll(clients);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Successfully assigned " + clients.size() + " client(s) to " + engineer.getFullName(),
                "assignedCount", clients.size(),
                "engineerName", engineer.getFullName()
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Assignment failed: " + e.getMessage()));
        }
    }

    /**
     * Get detailed engineer performance metrics
     */
    @GetMapping("/engineer-performance")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getEngineerPerformance() {
        try {
            // FIX: Using .contains("ENGINEER")
            List<User> engineers = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream()
                    .anyMatch(r -> r.getName().contains("ENGINEER")))
                .collect(Collectors.toList());

            List<Map<String, Object>> performance = engineers.stream().map(engineer -> {
                Map<String, Object> metrics = new HashMap<>();
                metrics.put("id", engineer.getId());
                metrics.put("name", engineer.getFullName());
                metrics.put("email", engineer.getEmail());
                
                // Get all clients assigned to this engineer
                List<User> assignedClients = userRepository.findAll().stream()
                    .filter(u -> u.getAssignedEngineer() != null)
                    .filter(u -> u.getAssignedEngineer().getId().equals(engineer.getId()))
                    .collect(Collectors.toList());
                
                metrics.put("totalAssigned", assignedClients.size());
                
                // FIX: Count completed surveys directly from database status
                long completed = assignedClients.stream()
                    .filter(c -> "COMPLETED".equals(c.getSurveyStatus()))
                    .count();
                
                metrics.put("completed", completed);
                metrics.put("inProgress", assignedClients.size() - completed);
                
                // Calculate completion rate
                double completionRate = assignedClients.isEmpty() ? 0.0 : 
                    (completed * 100.0) / assignedClients.size();
                metrics.put("completionRate", Math.round(completionRate));
                
                return metrics;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(performance);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to fetch performance data"));
        }
    }

    /**
     * Unassign client from engineer
     */
    @PostMapping("/unassign-client/{clientId}")
    @Transactional
    public ResponseEntity<?> unassignClient(@PathVariable Long clientId) {
        try {
            User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
            
            client.setAssignedEngineer(null);
            client.setSurveyStatus("PENDING"); // FIX: Reset status when unassigned
            userRepository.save(client);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Client unassigned successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    // ============ HELPER METHODS ============

    /**
     * Calculate client's survey status based on properties
     */
    private String calculateClientStatus(User client) {
        List<Property> properties = propertyRepository.findAll().stream()
            .filter(p -> p.getUser() != null && p.getUser().getId().equals(client.getId()))
            .collect(Collectors.toList());

        if (properties.isEmpty()) {
            return "NO_PROPERTY";
        }

        // 🔥 THE FIX: Stop overwriting the engineer! Read what the engineer actually saved in the DB.
        String dbStatus = client.getSurveyStatus();
        if (dbStatus != null && (dbStatus.equals("IN_PROGRESS") || dbStatus.equals("COMPLETED"))) {
            return dbStatus;
        }

        // Check if any property has completed survey (has floors with spaces)
        boolean hasCompletedSurvey = properties.stream()
            .anyMatch(this::hasCompletedSurvey);

        if (hasCompletedSurvey) {
            return "COMPLETED";
        }

        // Check if work is in progress (has properties but no completed survey)
        boolean hasStartedWork = properties.stream()
            .anyMatch(p -> hasAnyFloorPlans(p));

        if (hasStartedWork) {
            return "IN_PROGRESS";
        }

        if (client.getAssignedEngineer() != null) {
            return "ASSIGNED";
        }

        return "PENDING";
    }

    /**
     * Check if property has completed survey (has floors with imported spaces)
     */
    private boolean hasCompletedSurvey(Property property) {
        try {
            // FIX: Use java.lang.Number to prevent BigInteger to Long casting crash from MySQL
            Number spaceCount = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM spaces s " +
                "JOIN floors f ON s.floor_id = f.id " +
                "JOIN buildings b ON f.building_id = b.id " +
                "WHERE b.property_id = :propId")
                .setParameter("propId", property.getId())
                .getSingleResult();
            
            return spaceCount != null && spaceCount.longValue() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if property has any floor plans uploaded
     */
    private boolean hasAnyFloorPlans(Property property) {
        try {
            // FIX: Use java.lang.Number to prevent BigInteger to Long casting crash from MySQL
            Number fileCount = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM files f " +
                "JOIN floors fl ON f.id = fl.plan_file_id " +
                "JOIN buildings b ON fl.building_id = b.id " +
                "WHERE b.property_id = :propId")
                .setParameter("propId", property.getId())
                .getSingleResult();
            
            return fileCount != null && fileCount.longValue() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if client has completed any work
     */
    private boolean hasClientCompletedWork(User client) {
        List<Property> properties = propertyRepository.findAll().stream()
            .filter(p -> p.getUser() != null && p.getUser().getId().equals(client.getId()))
            .collect(Collectors.toList());

        return properties.stream().anyMatch(this::hasCompletedSurvey);
    }
}