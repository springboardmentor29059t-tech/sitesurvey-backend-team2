package com.survey.site.service;

import com.survey.site.model.Space;
import com.survey.site.repository.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

@Service
public class RFService {

    @Autowired
    private SpaceRepository spaceRepository;


    public String importRF(Long floorId, InputStream inputStream) {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {

                if (firstLine) { // skip header
                    firstLine = false;
                    continue;
                }

                String[] data = line.split(",");

                String spaceName = data[0].trim();
                Integer rfStrength = Integer.parseInt(data[1].trim());

                Space space = spaceRepository
                        .findByNameAndFloorId(spaceName, floorId)
                        .orElse(null);

                if (space != null) {
                    space.setRfStrength(rfStrength);
                    space.setRfStatus(getStatus(rfStrength));
                    spaceRepository.save(space);
                }
            }

            return "RF data imported successfully";

        } catch (Exception e) {
            e.printStackTrace();
            return "Import failed";
        }
    }


    public ByteArrayInputStream exportRF(Long floorId) {

        List<Space> spaces = spaceRepository.findByFloorId(floorId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        // header
        writer.println("spaceName,rfStrength,rfStatus");

        for (Space s : spaces) {
            writer.println(
                    s.getName() + "," +
                            (s.getRfStrength() != null ? s.getRfStrength() : "") + "," +
                            (s.getRfStatus() != null ? s.getRfStatus() : "")
            );
        }

        writer.flush();

        return new ByteArrayInputStream(out.toByteArray());
    }

    // ✅ RF STATUS LOGIC
    private String getStatus(int rf) {

        if (rf >= -60) return "GOOD";
        if (rf >= -75) return "MEDIUM";
        return "POOR";
    }
}