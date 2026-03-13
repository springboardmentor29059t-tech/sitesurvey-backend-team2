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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CSVImportService {

    private final SpaceRepository spaceRepository;
    private final FloorRepository floorRepository;

    public Map<String, Object> previewCSV(MultipartFile file, Long floorId) throws IOException {
        log.info("Parsing CSV file: {} for floor: {}", file.getOriginalFilename(), floorId);
        
        List<Map<String, String>> rows = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int rowNum = 0;

        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            for (CSVRecord record : csvParser) {
                rowNum++;
                Map<String, String> row = new HashMap<>();
                
                try {
                    row.put("name", record.get("name"));
                    row.put("type", record.get("type"));
                    
                    // Handle areaSqm
                    String areaValue = "";
                    if (record.isMapped("areaSqm")) {
                        areaValue = record.get("areaSqm");
                    } else if (record.isMapped("area_sqm")) {
                        areaValue = record.get("area_sqm");
                    } else {
                        throw new IllegalArgumentException("Mapping for areaSqm or area_sqm not found");
                    }
                    row.put("area_sqm", areaValue);
                    
                    row.put("notes", record.isMapped("notes") ? record.get("notes") : "");

                    // ✅ NEW: Safely read elevation_m and geometry_wkt
                    row.put("elevation_m", record.isMapped("elevation_m") ? record.get("elevation_m") : "");
                    row.put("geometry_wkt", record.isMapped("geometry_wkt") ? record.get("geometry_wkt") : "");
                    
                    if (row.get("name").trim().isEmpty()) {
                        errors.add("Row " + rowNum + ": Name is required");
                    }
                    
                    BigDecimal area = new BigDecimal(row.get("area_sqm"));
                    if (area.compareTo(BigDecimal.ZERO) <= 0) {
                        errors.add("Row " + rowNum + ": Area must be positive");
                    }
                    
                    rows.add(row);
                    
                } catch (Exception e) {
                    errors.add("Row " + rowNum + ": " + e.getMessage());
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

    @Transactional
    public int importSpaces(Long floorId, List<Map<String, String>> rows) {
        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> new RuntimeException("Floor not found: " + floorId));

        int count = 0;
        for (Map<String, String> row : rows) {
            Space space = new Space();
            space.setFloor(floor);
            space.setName(row.get("name"));
            space.setType(row.get("type"));
            space.setAreaSqm(new BigDecimal(row.get("area_sqm")));
            space.setNotes(row.get("notes"));

            // ✅ NEW: Save elevation_m if it exists
            if (row.containsKey("elevation_m") && !row.get("elevation_m").trim().isEmpty()) {
                try {
                    space.setElevationM(new BigDecimal(row.get("elevation_m").trim()));
                } catch (NumberFormatException e) {
                    log.warn("Invalid elevation value: {}", row.get("elevation_m"));
                }
            }

            // ✅ NEW: Save geometry_wkt if it exists
            if (row.containsKey("geometry_wkt") && !row.get("geometry_wkt").trim().isEmpty()) {
                space.setGeometryWkt(row.get("geometry_wkt").trim());
            }
            
            spaceRepository.save(space);
            count++;
        }

        log.info("✅ Imported {} spaces to floor {}", count, floorId);
        return count;
    }
}