package com.isp.sitesurvey.controller;

import com.isp.sitesurvey.entity.Property;
import com.isp.sitesurvey.entity.PropertyImage;
import com.isp.sitesurvey.entity.User;
import com.isp.sitesurvey.repository.PropertyImageRepository;
import com.isp.sitesurvey.repository.PropertyRepository;
import com.isp.sitesurvey.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
public class PropertyController {

    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository propertyImageRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new RuntimeException("No authentication found");
        String identifier = auth.getName();
        return userRepository.findByEmail(identifier)
                .orElseGet(() -> userRepository.findByUsername(identifier)
                .orElseThrow(() -> new RuntimeException("User not found: " + identifier)));
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getProperties() {
        try {
            User user = getCurrentUser();

            // FIX: JPQL projection - never load image_data blobs into the list response
            List<Object[]> rows = entityManager.createQuery(
                "SELECT p.id, p.name, p.city, p.state, p.country, p.imageContentType, " +
                "(CASE WHEN p.imageData IS NOT NULL THEN true ELSE false END) " +
                "FROM Property p WHERE p.user.id = :userId", Object[].class)
                .setParameter("userId", user.getId())
                .getResultList();

            List<Map<String, Object>> response = new ArrayList<>();
            for (Object[] row : rows) {
                Long propertyId = ((Number) row[0]).longValue();

                // FIX: Only fetch IDs from property_images, not the blob data
                List<Long> extraImageIds = entityManager.createQuery(
                    "SELECT pi.id FROM PropertyImage pi WHERE pi.property.id = :pid", Long.class)
                    .setParameter("pid", propertyId)
                    .getResultList();

                Map<String, Object> map = new HashMap<>();
                map.put("id", propertyId);
                map.put("name", row[1]);
                map.put("city", row[2]);
                map.put("state", row[3]);
                map.put("country", row[4]);
                map.put("imageContentType", row[5]);
                map.put("hasImage", Boolean.TRUE.equals(row[6]));
                map.put("extraImageIds", extraImageIds);
                response.add(map);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(consumes = {"multipart/form-data"})
    @Transactional
    public ResponseEntity<?> addProperty(
            @RequestParam("name") String name,
            @RequestParam("city") String city,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "country", required = false, defaultValue = "USA") String country,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            Property property = new Property();
            property.setName(name);
            property.setCity(city);
            property.setState(state);
            property.setCountry(country);
            property.setUser(getCurrentUser());

            if (image != null && !image.isEmpty()) {
                property.setImageData(image.getBytes());
                property.setImageContentType(image.getContentType());
            }

            Property saved = propertyRepository.save(property);
            return ResponseEntity.ok(Map.of(
                "id", saved.getId(),
                "name", saved.getName(),
                "city", saved.getCity(),
                "state", saved.getState() != null ? saved.getState() : "",
                "country", saved.getCountry(),
                "hasImage", saved.getImageData() != null,
                "imageContentType", saved.getImageContentType() != null ? saved.getImageContentType() : ""
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/image")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> getPropertyImage(@PathVariable Long id) {
        try {
            Optional<Property> propertyOpt = propertyRepository.findById(id);
            if (propertyOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Property property = propertyOpt.get();

            if (property.getImageData() == null || property.getImageData().length == 0) {
                return ResponseEntity.notFound().build();
            }

            String contentType = property.getImageContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = MediaType.IMAGE_JPEG_VALUE;
            }

            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .body(property.getImageData());

        } catch (Exception e) {
            System.err.println("INTERNAL ERROR fetching property image: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    @Transactional
    public ResponseEntity<?> updateProperty(
            @PathVariable Long id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            Property existing = propertyRepository.findById(id).orElseThrow();
            if (name != null) existing.setName(name);
            if (city != null) existing.setCity(city);
            if (state != null) existing.setState(state);
            if (country != null) existing.setCountry(country);

            if (image != null && !image.isEmpty()) {
                existing.setImageData(image.getBytes());
                existing.setImageContentType(image.getContentType());
            }

            Property saved = propertyRepository.save(existing);
            return ResponseEntity.ok(Map.of(
                "id", saved.getId(),
                "name", saved.getName(),
                "city", saved.getCity(),
                "state", saved.getState() != null ? saved.getState() : "",
                "country", saved.getCountry(),
                "hasImage", saved.getImageData() != null,
                "imageContentType", saved.getImageContentType() != null ? saved.getImageContentType() : ""
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteProperty(@PathVariable Long id) {
        try {
            propertyRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Property deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ==========================================
    // MULTIPLE IMAGES FEATURE
    // ==========================================

    @PostMapping(value = "/{id}/extra-images", consumes = {"multipart/form-data"})
    @Transactional
    public ResponseEntity<?> addExtraImage(@PathVariable Long id, @RequestParam("image") MultipartFile image) {
        try {
            Property property = propertyRepository.findById(id).orElseThrow();
            PropertyImage propImage = new PropertyImage();
            propImage.setProperty(property);
            propImage.setImageData(image.getBytes());
            propImage.setContentType(image.getContentType());
            PropertyImage saved = propertyImageRepository.save(propImage);
            return ResponseEntity.ok(Map.of("id", saved.getId(), "message", "Extra image added successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/extra-images/{imageId}")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> getExtraImage(@PathVariable Long imageId) {
        try {
            PropertyImage image = propertyImageRepository.findById(imageId).orElseThrow();

            if (image.getImageData() == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .header("Content-Type", image.getContentType() != null ? image.getContentType() : "image/jpeg")
                    .body(image.getImageData());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/extra-images/{imageId}")
    @Transactional
    public ResponseEntity<?> deleteExtraImage(@PathVariable Long imageId) {
        try {
            propertyImageRepository.deleteById(imageId);
            return ResponseEntity.ok(Map.of("message", "Extra image deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ==========================================
    // SPACES VIEW FOR CLIENT
    // ==========================================

    @GetMapping("/{id}/spaces")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPropertySpaces(@PathVariable Long id) {
        try {
            String sql = "SELECT id, name, type, area_sqm, notes FROM spaces WHERE floor_id = :id " +
                         "UNION SELECT s.id, s.name, s.type, s.area_sqm, s.notes FROM spaces s " +
                         "JOIN floors f ON s.floor_id = f.id JOIN buildings b ON f.building_id = b.id WHERE b.property_id = :id";
            List<Object[]> results = entityManager.createNativeQuery(sql).setParameter("id", id).getResultList();
            List<Map<String, Object>> spaces = new ArrayList<>();
            for (Object[] row : results) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", row[0]);
                map.put("name", row[1] != null ? row[1] : "Unnamed");
                map.put("type", row[2] != null ? row[2] : "General");
                map.put("area", row[3] != null ? row[3] : "0.00");
                map.put("floorLevel", "Imported Level");
                spaces.add(map);
            }
            return ResponseEntity.ok(spaces);
        } catch (Exception e) {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }
}