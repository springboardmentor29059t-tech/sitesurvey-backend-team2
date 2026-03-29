package com.isp.sitesurvey.controller;

import com.isp.sitesurvey.entity.Equipment;
import com.isp.sitesurvey.entity.Property;
import com.isp.sitesurvey.repository.EquipmentRepository;
import com.isp.sitesurvey.repository.PropertyRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/properties/{propertyId}/equipment")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EquipmentController {

    private final EquipmentRepository equipmentRepository;
    private final PropertyRepository  propertyRepository;

    // ── GET all equipment for map ────────────────────────────────────
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Equipment>> getEquipmentForProperty(@PathVariable Long propertyId) {
        return ResponseEntity.ok(equipmentRepository.findByPropertyId(propertyId));
    }

    // ── POST new device ──────────────────────────────────────────────
    @PostMapping
    @Transactional
    public ResponseEntity<?> addEquipment(
            @PathVariable Long propertyId,
            @RequestBody Map<String, Object> payload) {
        try {
            Property property = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new RuntimeException("Property not found"));

            Equipment equipment = new Equipment();
            equipment.setProperty(property);
            equipment.setType((String) payload.get("type"));
            equipment.setGeometryWkt((String) payload.get("geometryWkt"));

            if (payload.get("powerWatts") != null) {
                equipment.setPowerWatts(Integer.parseInt(payload.get("powerWatts").toString()));
            }

            return ResponseEntity.ok(equipmentRepository.save(equipment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── DELETE single device by ID ───────────────────────────────────
    // Called by SurveyCanvas when the engineer clicks "Remove Device"
    @DeleteMapping("/{equipmentId}")
    @Transactional
    public ResponseEntity<?> deleteEquipment(
            @PathVariable Long propertyId,
            @PathVariable Long equipmentId) {
        try {
            Equipment existing = equipmentRepository.findById(equipmentId)
                    .orElseThrow(() -> new RuntimeException("Equipment not found: " + equipmentId));

            // Safety check: ensure the device belongs to this property
            if (!existing.getProperty().getId().equals(propertyId)) {
                return ResponseEntity.status(403).body(Map.of("error", "Device does not belong to this property"));
            }

            equipmentRepository.delete(existing);
            return ResponseEntity.ok(Map.of("message", "Device removed", "id", equipmentId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── DELETE all equipment for a property ──────────────────────────
    @DeleteMapping
    @Transactional
    public ResponseEntity<?> clearEquipment(@PathVariable Long propertyId) {
        List<Equipment> existing = equipmentRepository.findByPropertyId(propertyId);
        equipmentRepository.deleteAll(existing);
        return ResponseEntity.ok(Map.of("message", "Cleared " + existing.size() + " devices"));
    }
}