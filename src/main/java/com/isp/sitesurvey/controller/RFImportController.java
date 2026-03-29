package com.isp.sitesurvey.controller;

import com.isp.sitesurvey.entity.Equipment;
import com.isp.sitesurvey.entity.Property;
import com.isp.sitesurvey.repository.EquipmentRepository;
import com.isp.sitesurvey.repository.PropertyRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * RFImportController — Milestone 4 RF tool integration
 *
 * FIX (coordinate normalisation): Real GPS coordinates are normalised into
 * canvas coordinate space before saving. See toCanvasWkt() for details.
 *
 * FIX (column aliases): Added "rssidbm" alias so Vistumbler CSVs that use the
 * column header "RSSIdbm" are correctly mapped to signal strength.
 *
 * FIX (rf-measurements endpoint): Added GET /api/rf/measurements/{propertyId}
 * which returns all stored equipment for a property as RF measurement points
 * { lng, lat, rssi, type, equipmentId }.  SurveyCanvas calls this to populate
 * the real-data heatmap layer.
 */
@RestController
@RequestMapping("/api/rf")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
public class RFImportController {

    private final EquipmentRepository equipmentRepository;
    private final PropertyRepository  propertyRepository;
    private final EntityManager       entityManager;

    // Canvas virtual coordinate bounds (must match SurveyCanvas.jsx FP object)
    private static final double CV_LNG_MIN = -0.016;
    private static final double CV_LNG_MAX =  0.016;
    private static final double CV_LAT_MIN = -0.009;
    private static final double CV_LAT_MAX =  0.009;

    /* ════════════════════════════════════════════════════════════════════
       COORDINATE NORMALISATION
    ════════════════════════════════════════════════════════════════════ */

    private static class GeoBounds {
        double latMin, latMax, lngMin, lngMax;
        boolean valid() {
            return latMax > latMin && lngMax > lngMin;
        }
    }

    @SuppressWarnings("unchecked")
    private GeoBounds loadGeoBounds(Long propertyId) {
        try {
            List<String> wkts = entityManager.createNativeQuery(
                "SELECT s.geometry_wkt FROM spaces s " +
                "JOIN floors f ON s.floor_id = f.id " +
                "JOIN buildings b ON f.building_id = b.id " +
                "WHERE b.property_id = :pid AND s.geometry_wkt IS NOT NULL")
                .setParameter("pid", propertyId).getResultList();

            if (!wkts.isEmpty()) {
                GeoBounds b = new GeoBounds();
                b.latMin = Double.MAX_VALUE; b.latMax = -Double.MAX_VALUE;
                b.lngMin = Double.MAX_VALUE; b.lngMax = -Double.MAX_VALUE;
                for (String wkt : wkts) {
                    double[][] coords = parsePolygonWkt(wkt);
                    for (double[] c : coords) {
                        b.lngMin = Math.min(b.lngMin, c[0]);
                        b.lngMax = Math.max(b.lngMax, c[0]);
                        b.latMin = Math.min(b.latMin, c[1]);
                        b.latMax = Math.max(b.latMax, c[1]);
                    }
                }
                if (b.valid()) return b;
            }
        } catch (Exception ignored) {}

        try {
            List<Equipment> existing = equipmentRepository.findByPropertyId(propertyId);
            if (!existing.isEmpty()) {
                GeoBounds b = new GeoBounds();
                b.latMin = Double.MAX_VALUE; b.latMax = -Double.MAX_VALUE;
                b.lngMin = Double.MAX_VALUE; b.lngMax = -Double.MAX_VALUE;
                for (Equipment eq : existing) {
                    double[] c = parsePoint(eq.getGeometryWkt());
                    if (c.length < 2) continue;
                    b.lngMin = Math.min(b.lngMin, c[0]);
                    b.lngMax = Math.max(b.lngMax, c[0]);
                    b.latMin = Math.min(b.latMin, c[1]);
                    b.latMax = Math.max(b.latMax, c[1]);
                }
                if (b.valid() && (Math.abs(b.lngMin) > 1 || Math.abs(b.latMin) > 1)) return b;
            }
        } catch (Exception ignored) {}

        return null;
    }

    private double[] toCanvasCoords(double realLon, double realLat, GeoBounds b) {
        double lngSpan = CV_LNG_MAX - CV_LNG_MIN;
        double latSpan = CV_LAT_MAX - CV_LAT_MIN;
        double normX = (realLon - b.lngMin) / (b.lngMax - b.lngMin);
        double normY = (realLat - b.latMin) / (b.latMax - b.latMin);
        double canvasLng = CV_LNG_MIN + normX * lngSpan;
        double canvasLat = CV_LAT_MAX - normY * latSpan;
        return new double[]{canvasLng, canvasLat};
    }

    private String toCanvasWkt(double lon, double lat, GeoBounds bounds) {
        boolean isRealGps = Math.abs(lon) > 1 || Math.abs(lat) > 1;
        if (isRealGps && bounds != null && bounds.valid()) {
            double[] cv = toCanvasCoords(lon, lat, bounds);
            return String.format(Locale.US, "POINT(%.10f %.10f)", cv[0], cv[1]);
        }
        return String.format(Locale.US, "POINT(%.8f %.8f)", lon, lat);
    }

    /* ════════════════════════════════════════════════════════════════════
       RF MEASUREMENTS ENDPOINT
       Returns all equipment for a property as { lng, lat, rssi, type, equipmentId }
       so SurveyCanvas can populate the real-data heatmap layer.
    ════════════════════════════════════════════════════════════════════ */

    @GetMapping("/measurements/{propertyId}")
    public ResponseEntity<List<Map<String, Object>>> getRfMeasurements(
            @PathVariable Long propertyId) {

        List<Equipment> equipment = equipmentRepository.findByPropertyId(propertyId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Equipment eq : equipment) {
            double[] coords = parsePoint(eq.getGeometryWkt());
            if (coords.length < 2) continue;

            // Only include devices that actually emit RF (skip switches)
            String type = eq.getType() == null ? "ap" : eq.getType().toLowerCase();
            if (type.equals("switch")) continue;

            // Use stored RSSI if available, otherwise derive a plausible value from power
            double rssi = -60.0; // sensible fallback
            if (eq.getRssi() != null) {
                rssi = eq.getRssi().doubleValue();
            } else if (eq.getPowerWatts() != null) {
                // Rough approximation: higher power → stronger signal
                rssi = -45.0 - (20.0 - Math.min(eq.getPowerWatts(), 20)) * 1.5;
            }

            Map<String, Object> point = new LinkedHashMap<>();
            point.put("equipmentId", eq.getId());
            point.put("type",        type);
            point.put("lng",         coords[0]);
            point.put("lat",         coords[1]);
            point.put("rssi",        rssi);
            result.add(point);
        }

        return ResponseEntity.ok(result);
    }

    /* ════════════════════════════════════════════════════════════════════
       IMPORT ENDPOINTS
    ════════════════════════════════════════════════════════════════════ */

    @PostMapping("/import/vistumbler/{propertyId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> importVistumbler(
            @PathVariable Long propertyId,
            @RequestParam("file") MultipartFile file) {

        Property property = requireProperty(propertyId);
        GeoBounds bounds  = loadGeoBounds(propertyId);
        List<Map<String, Object>> imported = new ArrayList<>();
        List<String> skipped = new ArrayList<>();

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser csv = CSVFormat.DEFAULT
                     .withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim().parse(reader)) {

            for (CSVRecord record : csv) {
                try {
                    // FIX: added "latitude" / "longitude" (title-case aliases handled by
                    // withIgnoreHeaderCase) and the Vistumbler-specific "rssidbm" column name.
                    String lat   = firstPresent(record, "latitude",  "lat",  "gpslatitude");
                    String lon   = firstPresent(record, "longitude", "lon",  "gpslongitude");
                    String ssid  = firstPresent(record, "ssid", "networkname", "network name");
                    String bssid = firstPresent(record, "bssid", "mac");
                    String sig   = firstPresent(record, "rssidbm", "signal", "signalstrength", "rssi");
                    String chan  = firstPresent(record, "channel", "chan");
                    String type  = firstPresent(record, "type", "device_type", "networktype");

                    if (lat == null || lon == null) {
                        skipped.add("Row " + record.getRecordNumber() + ": missing coordinates");
                        continue;
                    }

                    double dLat = Double.parseDouble(lat.replace(",", "."));
                    double dLon = Double.parseDouble(lon.replace(",", "."));

                    if (Math.abs(dLat) > 90 || Math.abs(dLon) > 180) {
                        skipped.add("Row " + record.getRecordNumber() + ": coordinates out of range");
                        continue;
                    }

                    String wkt    = toCanvasWkt(dLon, dLat, bounds);
                    String eqType = resolveEquipmentType(type, "ap");

                    // Parse RSSI — Vistumbler stores it as a negative integer e.g. "-55"
                    Integer rssiInt = null;
                    if (sig != null && !sig.isBlank()) {
                        try { rssiInt = Integer.parseInt(sig.trim()); } catch (NumberFormatException ignored) {}
                    }

                    Equipment eq = buildEquipment(property, eqType, wkt, 12, rssiInt);
                    eq = equipmentRepository.save(eq);

                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id",      eq.getId());
                    row.put("ssid",    nvl(ssid,  "Unknown"));
                    row.put("bssid",   nvl(bssid, "—"));
                    row.put("signal",  sig != null ? sig + " dBm" : "—");
                    row.put("channel", nvl(chan,   "—"));
                    row.put("lat",     dLat);
                    row.put("lon",     dLon);
                    imported.add(row);

                } catch (NumberFormatException e) {
                    skipped.add("Row " + record.getRecordNumber() + ": invalid number — " + e.getMessage());
                }
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to parse Vistumbler file: " + e.getMessage()));
        }

        return ResponseEntity.ok(buildImportResult("vistumbler", imported, skipped));
    }

    @PostMapping("/import/kismet/{propertyId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> importKismet(
            @PathVariable Long propertyId,
            @RequestParam("file") MultipartFile file) {

        Property property = requireProperty(propertyId);
        GeoBounds bounds  = loadGeoBounds(propertyId);
        List<Map<String, Object>> imported = new ArrayList<>();
        List<String> skipped = new ArrayList<>();

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = dbf.newDocumentBuilder().parse(file.getInputStream());
            doc.getDocumentElement().normalize();
            NodeList networks = doc.getElementsByTagName("wireless-network");

            for (int i = 0; i < networks.getLength(); i++) {
                Element net = (Element) networks.item(i);
                try {
                    String ssid  = textContent(net, "essid");
                    String bssid = textContent(net, "BSSID");
                    String chan  = textContent(net, "channel");
                    String sig   = textContent(net, "last_signal_dbm");

                    NodeList gpsNodes = net.getElementsByTagName("gps-info");
                    if (gpsNodes.getLength() == 0) {
                        skipped.add("Network " + nvl(bssid, "#" + i) + ": no GPS info");
                        continue;
                    }
                    Element gps = (Element) gpsNodes.item(0);
                    String latStr = textContent(gps, "avg-lat");
                    String lonStr = textContent(gps, "avg-lon");
                    if (latStr == null) latStr = textContent(gps, "peak-lat");
                    if (lonStr == null) lonStr = textContent(gps, "peak-lon");

                    if (latStr == null || lonStr == null) {
                        skipped.add("Network " + nvl(bssid, "#" + i) + ": missing lat/lon");
                        continue;
                    }

                    double dLat = Double.parseDouble(latStr);
                    double dLon = Double.parseDouble(lonStr);
                    String wkt  = toCanvasWkt(dLon, dLat, bounds);

                    Integer rssiInt = null;
                    if (sig != null) { try { rssiInt = Integer.parseInt(sig.trim()); } catch (NumberFormatException ignored) {} }

                    Equipment eq = buildEquipment(property, "ap", wkt, 12, rssiInt);
                    eq = equipmentRepository.save(eq);

                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id",      eq.getId());
                    row.put("ssid",    nvl(ssid,  "Hidden"));
                    row.put("bssid",   nvl(bssid, "—"));
                    row.put("signal",  nvl(sig,   "—") + " dBm");
                    row.put("channel", nvl(chan,   "—"));
                    row.put("lat",     dLat);
                    row.put("lon",     dLon);
                    imported.add(row);

                } catch (Exception e) {
                    skipped.add("Network #" + i + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to parse Kismet file: " + e.getMessage()));
        }

        return ResponseEntity.ok(buildImportResult("kismet", imported, skipped));
    }

    @PostMapping("/import/splat/{propertyId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> importSplat(
            @PathVariable Long propertyId,
            @RequestParam("file") MultipartFile file) {

        Property property = requireProperty(propertyId);
        GeoBounds bounds  = loadGeoBounds(propertyId);
        List<Map<String, Object>> imported = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        String filename = nvl(file.getOriginalFilename(), "").toLowerCase();

        try {
            if (filename.endsWith(".qth")) {
                BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
                String siteName = br.readLine();
                String latLine  = br.readLine();
                String lonLine  = br.readLine();
                br.close();

                if (latLine == null || lonLine == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid .qth file format"));
                }

                double dLat = Double.parseDouble(latLine.trim());
                double dLon = Double.parseDouble(lonLine.trim());
                String wkt  = toCanvasWkt(dLon, dLat, bounds);

                Equipment eq = buildEquipment(property, "router", wkt, 15, null);
                eq = equipmentRepository.save(eq);

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id",   eq.getId());
                row.put("name", nvl(siteName, "SPLAT Site"));
                row.put("lat",  dLat);
                row.put("lon",  dLon);
                imported.add(row);

            } else {
                Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
                CSVParser csv = CSVFormat.DEFAULT.withFirstRecordAsHeader()
                        .withIgnoreHeaderCase().withTrim().parse(reader);

                for (CSVRecord record : csv) {
                    try {
                        String lat  = firstPresent(record, "lat", "latitude");
                        String lon  = firstPresent(record, "lon", "longitude");
                        String type = firstPresent(record, "type", "device_type");
                        String name = firstPresent(record, "name", "site_name");

                        if (lat == null || lon == null) {
                            skipped.add("Row " + record.getRecordNumber() + ": missing coords");
                            continue;
                        }

                        double dLat   = Double.parseDouble(lat);
                        double dLon   = Double.parseDouble(lon);
                        String wkt    = toCanvasWkt(dLon, dLat, bounds);
                        String eqType = nvl(type, "router");

                        Equipment eq = buildEquipment(property, eqType, wkt, 15, null);
                        eq = equipmentRepository.save(eq);

                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("id",   eq.getId());
                        row.put("name", nvl(name, "Site"));
                        row.put("lat",  dLat);
                        row.put("lon",  dLon);
                        imported.add(row);

                    } catch (NumberFormatException e) {
                        skipped.add("Row " + record.getRecordNumber() + ": " + e.getMessage());
                    }
                }
                csv.close();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to parse SPLAT! file: " + e.getMessage()));
        }

        return ResponseEntity.ok(buildImportResult("splat", imported, skipped));
    }

    @PostMapping("/import/generic/{propertyId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> importGenericCsv(
            @PathVariable Long propertyId,
            @RequestParam("file") MultipartFile file) {

        Property property = requireProperty(propertyId);
        GeoBounds bounds  = loadGeoBounds(propertyId);
        List<Map<String, Object>> imported = new ArrayList<>();
        List<String> skipped = new ArrayList<>();

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser csv = CSVFormat.DEFAULT.withFirstRecordAsHeader()
                     .withIgnoreHeaderCase().withTrim().parse(reader)) {

            for (CSVRecord record : csv) {
                try {
                    String lat  = firstPresent(record, "lat", "latitude",  "y");
                    String lon  = firstPresent(record, "lon", "longitude", "x", "lng");
                    String type = firstPresent(record, "type", "device_type", "kind");
                    String sig  = firstPresent(record, "rssidbm", "signal", "signalstrength", "rssi");
                    if (lat == null || lon == null) {
                        skipped.add("Row " + record.getRecordNumber());
                        continue;
                    }

                    double dLat   = Double.parseDouble(lat);
                    double dLon   = Double.parseDouble(lon);
                    String wkt    = toCanvasWkt(dLon, dLat, bounds);
                    String eqType = resolveEquipmentType(nvl(type, "ap"), "ap");

                    Integer rssiInt = null;
                    if (sig != null) { try { rssiInt = Integer.parseInt(sig.trim()); } catch (NumberFormatException ignored) {} }

                    Equipment eq = buildEquipment(property, eqType, wkt, 12, rssiInt);
                    eq = equipmentRepository.save(eq);
                    imported.add(Map.of("id", eq.getId(), "lat", dLat,
                                        "lon", dLon, "type", eqType));

                } catch (Exception e) {
                    skipped.add("Row " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

        return ResponseEntity.ok(buildImportResult("generic", imported, skipped));
    }

    /* ════════════════════════════════════════════════════════════════════
       EXPORT ENDPOINTS
    ════════════════════════════════════════════════════════════════════ */

    @GetMapping("/export/{propertyId}/csv")
    public ResponseEntity<byte[]> exportCsv(@PathVariable Long propertyId) {
        List<Equipment> equipment = equipmentRepository.findByPropertyId(propertyId);
        try (StringWriter sw = new StringWriter();
             CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT
                     .withHeader("id","type","longitude","latitude","rssi","power_watts","geometry_wkt"))) {
            for (Equipment eq : equipment) {
                double[] coords = parsePoint(eq.getGeometryWkt());
                printer.printRecord(eq.getId(), eq.getType(),
                        coords.length > 1 ? coords[0] : 0,
                        coords.length > 1 ? coords[1] : 0,
                        eq.getRssi(),
                        eq.getPowerWatts(), eq.getGeometryWkt());
            }
            printer.flush();
            byte[] bytes = sw.toString().getBytes(StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"equipment_" + propertyId + ".csv\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/export/{propertyId}/geojson")
    public ResponseEntity<String> exportGeoJson(@PathVariable Long propertyId) {
        List<Equipment> equipment = equipmentRepository.findByPropertyId(propertyId);
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"FeatureCollection\",\"features\":[");
        for (int i = 0; i < equipment.size(); i++) {
            Equipment eq = equipment.get(i);
            double[] c = parsePoint(eq.getGeometryWkt());
            sb.append("{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[")
              .append(String.format(Locale.US, "%.8f,%.8f",
                      c.length > 1 ? c[0] : 0, c.length > 1 ? c[1] : 0))
              .append("]},\"properties\":{\"id\":").append(eq.getId())
              .append(",\"type\":\"").append(eq.getType()).append("\"")
              .append(",\"rssi\":").append(eq.getRssi() != null ? eq.getRssi() : "null")
              .append(",\"powerWatts\":").append(eq.getPowerWatts() != null ? eq.getPowerWatts() : 0)
              .append("}}");
            if (i < equipment.size() - 1) sb.append(",");
        }
        sb.append("]}");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"equipment_" + propertyId + ".geojson\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(sb.toString());
    }

    @GetMapping("/export/{propertyId}/kml")
    public ResponseEntity<byte[]> exportKml(@PathVariable Long propertyId) {
        var propertyOpt = propertyRepository.findById(propertyId);
        String propName = propertyOpt.map(Property::getName).orElse("Property " + propertyId);
        List<Equipment> equipment = equipmentRepository.findByPropertyId(propertyId);

        StringBuilder kml = new StringBuilder();
        kml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        kml.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n<Document>\n");
        kml.append("<name>").append(escapeXml(propName)).append(" — Site Survey</name>\n");
        for (String[] s : new String[][]{
                {"router","38bdf8"}, {"switch","a78bfa"}, {"ap","34d399"}}) {
            kml.append("<Style id=\"").append(s[0]).append("\">")
               .append("<IconStyle><color>ff").append(s[1]).append("</color>")
               .append("<Icon><href>http://maps.google.com/mapfiles/kml/paddle/wht-circle.png</href></Icon>")
               .append("</IconStyle></Style>\n");
        }
        for (Equipment eq : equipment) {
            double[] c = parsePoint(eq.getGeometryWkt());
            kml.append("<Placemark>")
               .append("<name>").append(escapeXml(eq.getType().toUpperCase()))
               .append(" #").append(eq.getId()).append("</name>")
               .append("<styleUrl>#").append(eq.getType().toLowerCase()).append("</styleUrl>")
               .append("<Point><coordinates>")
               .append(String.format(Locale.US, "%.8f,%.8f,0",
                       c.length > 1 ? c[0] : 0, c.length > 1 ? c[1] : 0))
               .append("</coordinates></Point></Placemark>\n");
        }
        kml.append("</Document>\n</kml>");
        byte[] bytes = kml.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"survey_" + propertyId + ".kml\"")
                .contentType(MediaType.parseMediaType("application/vnd.google-earth.kml+xml"))
                .body(bytes);
    }

    /* ─── Helpers ─────────────────────────────────────────────────────── */

    private Property requireProperty(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found: " + id));
    }

    /**
     * Map common CSV type strings (infrastructure, router, ap, access_point…)
     * to the three canonical equipment types used by the canvas.
     */
    private String resolveEquipmentType(String raw, String fallback) {
        if (raw == null || raw.isBlank()) return fallback;
        String lower = raw.strip().toLowerCase();
        if (lower.contains("router") || lower.contains("gateway")) return "router";
        if (lower.contains("switch")) return "switch";
        // "infrastructure", "ap", "access_point", "access point" → ap
        return "ap";
    }

    private Equipment buildEquipment(Property p, String type, String wkt, int power, Integer rssi) {
        Equipment eq = new Equipment();
        eq.setProperty(p);
        eq.setType(type.toLowerCase());
        eq.setGeometryWkt(wkt);
        eq.setPowerWatts(power);
        if (rssi != null) eq.setRssi(rssi);
        return eq;
    }

    private Map<String, Object> buildImportResult(String source,
                                                   List<Map<String, Object>> imported,
                                                   List<String> skipped) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("source",        source);
        result.put("importedCount", imported.size());
        result.put("skippedCount",  skipped.size());
        result.put("imported",      imported);
        result.put("skipped",       skipped);
        return result;
    }

    private String firstPresent(CSVRecord record, String... keys) {
        for (String key : keys) {
            try {
                String val = record.get(key);
                if (val != null && !val.isBlank()) return val.trim();
            } catch (IllegalArgumentException ignored) {}
        }
        return null;
    }

    private String textContent(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() == 0) return null;
        String text = nodes.item(0).getTextContent();
        return (text == null || text.isBlank()) ? null : text.trim();
    }

    private double[] parsePoint(String wkt) {
        try {
            String inner = wkt.replaceAll("(?i)POINT\\s*\\(", "").replace(")", "").trim();
            String[] parts = inner.split("\\s+");
            return new double[]{Double.parseDouble(parts[0]), Double.parseDouble(parts[1])};
        } catch (Exception e) { return new double[]{0, 0}; }
    }

    private double[][] parsePolygonWkt(String wkt) {
        try {
            String inner = wkt.replaceAll("(?i)POLYGON\\s*\\(\\s*\\(", "")
                              .replaceAll("\\)\\s*\\)\\s*$", "");
            String[] pairs = inner.split(",");
            double[][] coords = new double[pairs.length][2];
            for (int i = 0; i < pairs.length; i++) {
                String[] xy = pairs[i].trim().split("\\s+");
                coords[i][0] = Double.parseDouble(xy[0]);
                coords[i][1] = Double.parseDouble(xy[1]);
            }
            return coords;
        } catch (Exception e) { return new double[0][0]; }
    }

    private String nvl(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }

    private String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}