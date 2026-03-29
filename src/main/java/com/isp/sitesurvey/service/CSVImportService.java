package com.isp.sitesurvey.service;

import com.isp.sitesurvey.entity.Floor;
import com.isp.sitesurvey.entity.Space;
import com.isp.sitesurvey.repository.FloorRepository;
import com.isp.sitesurvey.repository.SpaceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CSVImportService {

    private final SpaceRepository spaceRepository;
    private final FloorRepository floorRepository;

    // ======================================================
    // CSV PREVIEW (VALIDATION BEFORE IMPORT)
    // ======================================================

    public Map<String, Object> previewCSV(MultipartFile file, Long floorId) throws IOException {

        log.info("Preview CSV file {} for floor {}", file.getOriginalFilename(), floorId);

        List<Map<String, String>> rows = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        int rowNumber = 0;

        try (
                Reader reader = new InputStreamReader(file.getInputStream());
                CSVParser csvParser = new CSVParser(
                        reader,
                        CSVFormat.DEFAULT.builder()
                                .setHeader()
                                .setSkipHeaderRecord(true)
                                .setIgnoreHeaderCase(true)
                                .setTrim(true)
                                .build()
                )
        ) {

            for (CSVRecord record : csvParser) {

                rowNumber++;

                Map<String, String> row = new HashMap<>();

                try {

                    String name = record.get("name");
                    String type = record.get("type");

                    String areaValue = "";

                    if (record.isMapped("areaSqm")) {
                        areaValue = record.get("areaSqm");
                    }
                    else if (record.isMapped("area_sqm")) {
                        areaValue = record.get("area_sqm");
                    }
                    else {
                        throw new RuntimeException("CSV must contain column area_sqm or areaSqm");
                    }

                    String notes = record.isMapped("notes") ? record.get("notes") : "";
                    String elevation = record.isMapped("elevation_m") ? record.get("elevation_m") : "";
                    String geometry = record.isMapped("geometry_wkt") ? record.get("geometry_wkt") : "";

                    row.put("name", name);
                    row.put("type", type);
                    row.put("area_sqm", areaValue);
                    row.put("notes", notes);
                    row.put("elevation_m", elevation);
                    row.put("geometry_wkt", geometry);

                    // ---------------- VALIDATIONS ----------------

                    if (name == null || name.trim().isEmpty()) {
                        errors.add("Row " + rowNumber + ": name is required");
                    }

                    BigDecimal area = new BigDecimal(areaValue);

                    if (area.compareTo(BigDecimal.ZERO) <= 0) {
                        errors.add("Row " + rowNumber + ": area must be positive");
                    }

                    rows.add(row);

                }
                catch (Exception e) {

                    errors.add("Row " + rowNumber + ": " + e.getMessage());

                }
            }

        }

        Map<String, Object> result = new HashMap<>();

        result.put("rows", rows);
        result.put("errors", errors);
        result.put("totalRows", rows.size());
        result.put("valid", errors.isEmpty());

        return result;
    }

    // ======================================================
    // IMPORT SPACES INTO FLOOR
    // ======================================================

    @Transactional
    public int importSpaces(Long floorId, List<Map<String, String>> rows) {

        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> new RuntimeException("Floor not found: " + floorId));

        int importedCount = 0;

        for (Map<String, String> row : rows) {

            try {

                Space space = new Space();

                space.setFloor(floor);

                space.setName(row.get("name"));

                space.setType(row.get("type"));

                space.setAreaSqm(new BigDecimal(row.get("area_sqm")));

                space.setNotes(row.get("notes"));

                // ---------- elevation_m ----------

                if (row.containsKey("elevation_m")
                        && row.get("elevation_m") != null
                        && !row.get("elevation_m").trim().isEmpty()) {

                    try {
                        space.setElevationM(new BigDecimal(row.get("elevation_m")));
                    }
                    catch (Exception e) {
                        log.warn("Invalid elevation value: {}", row.get("elevation_m"));
                    }
                }

                // ---------- geometry_wkt ----------

                if (row.containsKey("geometry_wkt")
                        && row.get("geometry_wkt") != null
                        && !row.get("geometry_wkt").trim().isEmpty()) {

                    space.setGeometryWkt(row.get("geometry_wkt"));
                }

                spaceRepository.save(space);

                importedCount++;

            }
            catch (Exception e) {

                log.error("Error importing row: {}", row, e);

            }

        }

        log.info("Successfully imported {} spaces for floor {}", importedCount, floorId);

        return importedCount;
    }
}