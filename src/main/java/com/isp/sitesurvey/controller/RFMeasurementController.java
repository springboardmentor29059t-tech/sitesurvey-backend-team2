package com.isp.sitesurvey.controller;

import com.isp.sitesurvey.entity.Equipment;
import com.isp.sitesurvey.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * RFMeasurementController
 *
 * Serves GET /api/properties/{propertyId}/rf-measurements
 *
 * The SurveyCanvas heatmap calls this endpoint to get RF signal measurements.
 * Since we don't have a separate rf_measurements table, we derive signal
 * "measurements" from the placed equipment:
 *   - Each device with a rangeM > 0 (router, ap) becomes a virtual measurement
 *     point at its location with a synthetic RSSI value.
 *   - This is the "fallback model" made explicit — the canvas will use IDW
 *     interpolation on these points to render the heatmap.
 *
 * If you later add a real rf_measurements table, replace the body of
 * getMeasurements() with a repository query.
 *
 * Place at: src/main/java/com/isp/sitesurvey/controller/RFMeasurementController.java
 */
@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
public class RFMeasurementController {

    private final EquipmentRepository equipmentRepository;

    /**
     * GET /api/properties/{propertyId}/rf-measurements?floorId={floorId}
     *
     * Returns a list of RF measurement points derived from placed equipment.
     * Each point has: lng, lat, rssi (dBm), type
     *
     * The canvas RFHeatmap class uses these for IDW interpolation.
     * If no equipment exists, returns empty list (canvas falls back to model).
     */
    @GetMapping("/{propertyId}/rf-measurements")
    public ResponseEntity<List<Map<String, Object>>> getMeasurements(
            @PathVariable Long propertyId,
            @RequestParam(value = "floorId", required = false) Long floorId) {

        try {
            List<Equipment> equipment = equipmentRepository.findByPropertyId(propertyId);
            List<Map<String, Object>> measurements = new ArrayList<>();

            for (Equipment eq : equipment) {
                // Only RF-emitting devices contribute to heatmap
                if (eq.getGeometryWkt() == null) continue;
                String type = eq.getType() != null ? eq.getType().toLowerCase() : "";
                if ("switch".equals(type)) continue; // wired only

                double[] coords = parsePoint(eq.getGeometryWkt());
                if (coords.length < 2) continue;

                // Synthetic RSSI at device location (strong signal at source)
                // router: -28 dBm reference, ap: -26 dBm reference
                double rssiAtSource = "router".equals(type) ? -28.0 : -26.0;

                Map<String, Object> point = new LinkedHashMap<>();
                point.put("lng",  coords[0]);
                point.put("lat",  coords[1]);
                point.put("rssi", rssiAtSource);
                point.put("type", eq.getType());
                point.put("equipmentId", eq.getId());
                measurements.add(point);

                // Add surrounding sample points at estimated range boundary
                // (simulates measured data spread around the device)
                double rangeM = "router".equals(type) ? 30.0 : 25.0;
                // Approximate degrees per metre at this latitude
                double degPerMetre = 0.000009; // ~1m at equator in WGS84
                double r = rangeM * degPerMetre;
                double rssiAtEdge = rssiAtSource - 10 * 3.0 * Math.log10(rangeM);

                int samples = 8;
                for (int i = 0; i < samples; i++) {
                    double angle = (2 * Math.PI * i) / samples;
                    Map<String, Object> edgePoint = new LinkedHashMap<>();
                    edgePoint.put("lng",  coords[0] + r * Math.cos(angle));
                    edgePoint.put("lat",  coords[1] + r * Math.sin(angle));
                    edgePoint.put("rssi", rssiAtEdge);
                    edgePoint.put("type", eq.getType());
                    edgePoint.put("equipmentId", eq.getId());
                    measurements.add(edgePoint);
                }
            }

            return ResponseEntity.ok(measurements);

        } catch (Exception e) {
            // Return empty list on any error — canvas handles this gracefully
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    private double[] parsePoint(String wkt) {
        try {
            String inner = wkt.replaceAll("(?i)POINT\\s*\\(", "").replace(")", "").trim();
            String[] parts = inner.split("\\s+");
            return new double[]{Double.parseDouble(parts[0]), Double.parseDouble(parts[1])};
        } catch (Exception e) {
            return new double[0];
        }
    }
}