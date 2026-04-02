


package com.survey.site.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class ImportService {

    public void importSpaces(MultipartFile file) {

        try {

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(file.getInputStream()));

            String line;

            while ((line = reader.readLine()) != null) {

                String[] data = line.split(",");

                String name = data[0];
                String floorId = data[1];

                System.out.println("Space: " + name + " Floor: " + floorId);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}