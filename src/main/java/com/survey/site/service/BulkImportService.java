package com.survey.site.service;

import com.opencsv.CSVReader;
import com.survey.site.model.*;
import com.survey.site.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.util.*;

@Service
public class BulkImportService {

    @Autowired private PropertyRepository propertyRepo;
    @Autowired private BuildingRepository buildingRepo;
    @Autowired private FloorRepository floorRepo;
    @Autowired private SpaceRepository spaceRepo;


public String importCSV(MultipartFile file) {

    int success = 0;
    int fail = 0;

    try {

        CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()));
        List<String[]> rows = reader.readAll();

        System.out.println("TOTAL ROWS: " + rows.size());

        for (int i = 1; i < rows.size(); i++) {

            try {

                String[] row = rows.get(i);

                if (row.length < 6) {
                    fail++;
                    continue;
                }

                String propertyName = row[0].trim();
                String buildingName = row[1].trim();
                String floorName = row[2].trim();
                int floorNumber = Integer.parseInt(row[3].trim());
                String spaceName = row[4].trim();
                double area = Double.parseDouble(row[5].trim());

                // ✅ PROPERTY (find or create)
                Property property = propertyRepo.findAll().stream()
                        .filter(p -> p.getName().equalsIgnoreCase(propertyName))
                        .findFirst()
                        .orElseGet(() -> {
                            Property p = new Property();
                            p.setName(propertyName);
                            p.setLocation("Auto");
                            return propertyRepo.save(p);
                        });

                // ✅ BUILDING (find or create)
                Building building = buildingRepo.findAll().stream()
                        .filter(b -> b.getName().equalsIgnoreCase(buildingName)
                                && b.getProperty().getId().equals(property.getId()))
                        .findFirst()
                        .orElseGet(() -> {
                            Building b = new Building();
                            b.setName(buildingName);
                            b.setProperty(property);
                            return buildingRepo.save(b);
                        });

                // ✅ FLOOR (find or create)
                Floor floor = floorRepo.findByBuildingId(building.getId()).stream()
                        .filter(f -> f.getFloorNumber().equals(floorNumber))
                        .findFirst()
                        .orElseGet(() -> {
                            Floor f = new Floor();
                            f.setName(floorName);
                            f.setFloorNumber(floorNumber);
                            f.setBuilding(building);
                            return floorRepo.save(f);
                        });

                // ✅ SPACE (always create)
                Space space = new Space();
                space.setName(spaceName);
                space.setArea(area);
                space.setFloor(floor);

                spaceRepo.save(space);

                System.out.println("Saved: " + spaceName);
                success++;

            } catch (Exception e) {
                fail++;
                System.out.println("FAILED ROW " + i + ": " + e.getMessage());
            }
        }

        return "Imported Success: " + success + ", Failed: " + fail;

    } catch (Exception e) {
        e.printStackTrace();
        return "ERROR: " + e.getMessage();
    }
}


    public String importExcel(MultipartFile file) {

        int success = 0;
        int fail = 0;

        try {

            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            System.out.println("TOTAL ROWS: " + sheet.getLastRowNum());

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                try {

                    Row row = sheet.getRow(i);
                    if (row == null) {
                        fail++;
                        continue;
                    }

                    String propertyName = row.getCell(0).getStringCellValue().trim();
                    String buildingName = row.getCell(1).getStringCellValue().trim();
                    String floorName = row.getCell(2).getStringCellValue().trim();
                    int floorNumber = (int) row.getCell(3).getNumericCellValue();
                    String spaceName = row.getCell(4).getStringCellValue().trim();
                    double area = row.getCell(5).getNumericCellValue();


                    Property property = propertyRepo.findAll().stream()
                            .filter(p -> p.getName().equalsIgnoreCase(propertyName))
                            .findFirst()
                            .orElseGet(() -> {
                                Property p = new Property();
                                p.setName(propertyName);
                                p.setLocation("Auto");
                                return propertyRepo.save(p);
                            });


                    Building building = buildingRepo.findAll().stream()
                            .filter(b -> b.getName().equalsIgnoreCase(buildingName)
                                    && b.getProperty().getId().equals(property.getId()))
                            .findFirst()
                            .orElseGet(() -> {
                                Building b = new Building();
                                b.setName(buildingName);
                                b.setProperty(property);
                                return buildingRepo.save(b);
                            });

                    // ✅ FLOOR
                    Floor floor = floorRepo.findByBuildingId(building.getId()).stream()
                            .filter(f -> f.getFloorNumber().equals(floorNumber))
                            .findFirst()
                            .orElseGet(() -> {
                                Floor f = new Floor();
                                f.setName(floorName);
                                f.setFloorNumber(floorNumber);
                                f.setBuilding(building);
                                return floorRepo.save(f);
                            });

                    // ✅ SPACE
                    Space space = new Space();
                    space.setName(spaceName);
                    space.setArea(area);
                    space.setFloor(floor);

                    spaceRepo.save(space);

                    System.out.println("Saved: " + spaceName);
                    success++;

                } catch (Exception e) {
                    fail++;
                    System.out.println("FAILED ROW " + i + ": " + e.getMessage());
                }
            }

            return "Excel Imported Success: " + success + ", Failed: " + fail;

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }
    }
}