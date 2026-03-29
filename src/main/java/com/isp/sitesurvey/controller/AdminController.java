package com.isp.sitesurvey.controller;

import com.isp.sitesurvey.entity.Property;
import com.isp.sitesurvey.entity.User;
import com.isp.sitesurvey.entity.Role;
import com.isp.sitesurvey.repository.PropertyRepository;
import com.isp.sitesurvey.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final EntityManager entityManager;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository, PropertyRepository propertyRepository, 
                           EntityManager entityManager, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.entityManager = entityManager;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        
        List<Map<String, Object>> response = users.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("fullName", u.getFullName());
            map.put("email", u.getEmail());
            map.put("username", u.getUsername());
            map.put("phoneNumber", u.getPhoneNumber());
            map.put("isEnabled", u.getIsEnabled());
            
            // Extract role names specifically for the frontend badges
            List<String> roles = u.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
            map.put("roles", roles);
            
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}/toggle-lock")
    @Transactional
    public ResponseEntity<?> toggleUserLock(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"))) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot lock an Admin account"));
        }

        user.setIsEnabled(!user.getIsEnabled());
        userRepository.save(user);
        
        String status = user.getIsEnabled() ? "unlocked" : "locked";
        return ResponseEntity.ok(Map.of("message", "User account has been " + status));
    }

    @DeleteMapping("/users/{id}")
    @Transactional
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"))) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot delete an Admin account"));
        }

        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    // --- THE FIX: Fully Mapping the User Object for Properties ---
    @GetMapping("/properties")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getAllPropertiesGlobally() {
        try {
            List<Property> properties = propertyRepository.findAll();
            List<Map<String, Object>> result = properties.stream().map(p -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", p.getId());
                map.put("name", p.getName());
                map.put("city", p.getCity() != null ? p.getCity() : "N/A");
                map.put("state", p.getState() != null ? p.getState() : "");
                map.put("country", p.getCountry() != null ? p.getCountry() : "N/A");
                map.put("imageContentType", p.getImageContentType());
                
                // Properly map the User so the frontend doesn't say "Unknown"
                if (p.getUser() != null) {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", p.getUser().getId());
                    userMap.put("email", p.getUser().getEmail());
                    userMap.put("fullName", p.getUser().getFullName());
                    userMap.put("username", p.getUser().getUsername());
                    map.put("user", userMap);
                } else {
                    map.put("user", null);
                }
                
                return map;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/users")
    @Transactional
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> payload) {
        try {
            User user = new User();
            user.setEmail(payload.get("email"));
            user.setUsername(payload.get("username"));
            user.setFullName(payload.get("fullName"));
            user.setPhoneNumber(payload.get("phoneNumber"));
            user.setPasswordHash(passwordEncoder.encode(payload.get("password")));
            user.setIsEnabled(true);
            user.setIsAccountLocked(false);

            String roleName = payload.get("role");
            if (!roleName.startsWith("ROLE_")) roleName = "ROLE_" + roleName;
            
            Role role = (Role) entityManager.createQuery("SELECT r FROM Role r WHERE r.name = :name")
                    .setParameter("name", roleName)
                    .getSingleResult();
            
            java.util.Set<Role> roles = new java.util.HashSet<>();
            roles.add(role);
            user.setRoles(roles);

            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "User created successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to create user: " + e.getMessage()));
        }
    }

    @GetMapping("/properties/{id}/floors")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPropertyFloors(@PathVariable Long id) {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> results = entityManager.createNativeQuery(
                    "SELECT f.id, f.level_label, f.plan_file_id, b.name as building_name " +
                    "FROM floors f JOIN buildings b ON f.building_id = b.id " +
                    "WHERE b.property_id = :propId")
                    .setParameter("propId", id)
                    .getResultList();

            List<Map<String, Object>> floors = results.stream().map(row -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", row[0]);
                map.put("levelLabel", row[1]);
                map.put("planFileId", row[2]);
                map.put("buildingName", row[3]);
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(floors);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error fetching floors: " + e.getMessage()));
        }
    }

    @GetMapping("/floors/{floorId}/spaces")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getFloorSpaces(@PathVariable Long floorId) {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> results = entityManager.createNativeQuery(
                    "SELECT id, name, type, area_sqm, notes FROM spaces WHERE floor_id = :floorId")
                    .setParameter("floorId", floorId)
                    .getResultList();

            List<Map<String, Object>> spaces = results.stream().map(row -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", row[0]);
                map.put("name", row[1]);
                map.put("type", row[2] != null ? row[2] : "N/A");
                map.put("area", row[3] != null ? row[3] : "0.00");
                map.put("notes", row[4] != null ? row[4] : "");
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(spaces);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error fetching spaces: " + e.getMessage()));
        }
    }
}