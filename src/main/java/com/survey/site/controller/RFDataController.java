package com.survey.site.controller;

import com.survey.site.model.Space;
import com.survey.site.repository.SpaceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/rf")
@CrossOrigin("*")
public class RFDataController {

    @Autowired
    private SpaceRepository spaceRepository;

    // =========================
    // ✅ EXPORT CSV
    // =========================
    @GetMapping("/export/{floorId}")
    public void exportRF(@PathVariable Long floorId, HttpServletResponse response) throws IOException {

        List<Space> spaces = spaceRepository.findByFloorId(floorId);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=rf_data.csv");

        PrintWriter writer = response.getWriter();

        // Header
        writer.println("spaceName,rfStrength,rfStatus");

        for (Space s : spaces) {
            writer.println(
                    s.getName() + "," +
                            (s.getRfStrength() != null ? s.getRfStrength() : "") + "," +
                            (s.getRfStatus() != null ? s.getRfStatus() : "")
            );
        }

        writer.flush();
        writer.close();
    }

    // =========================
    // ✅ IMPORT CSV
    // =========================
    @PostMapping("/import/{floorId}")
    public String importRF(@PathVariable Long floorId,
                           @RequestParam("file") MultipartFile file) {

        try {

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream())
            );

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {

                // Skip header
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] data = line.split(",");

                if (data.length < 3) continue;

                String spaceName = data[0];
                String rfStrengthStr = data[1];
                String rfStatus = data[2];

                Optional<Space> optionalSpace =
                        spaceRepository.findByNameAndFloorId(spaceName, floorId);

                if (optionalSpace.isPresent()) {

                    Space space = optionalSpace.get();

                    // Update RF
                    if (!rfStrengthStr.isEmpty()) {
                        space.setRfStrength(Integer.parseInt(rfStrengthStr));
                    }

                    space.setRfStatus(rfStatus);

                    spaceRepository.save(space);
                }
            }

            return "RF Data Imported Successfully";

        } catch (Exception e) {
            e.printStackTrace();
            return "Import Failed";
        }
    }
}