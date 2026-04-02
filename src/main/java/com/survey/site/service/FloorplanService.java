
package com.survey.site.service;

import com.survey.site.model.Floor;
import com.survey.site.model.Floorplan;
import com.survey.site.repository.FloorRepository;
import com.survey.site.repository.FloorplanRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class FloorplanService {

    @Autowired
    private FloorplanRepository floorplanRepository;

    @Autowired
    private FloorRepository floorRepository;

    /* ---------- UPLOAD FLOORPLAN ---------- */

    public Floorplan uploadFloorplan(
            String name,
            String description,
            Long floorId,
            MultipartFile file
    ) throws IOException {

        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> new RuntimeException("Floor not found with ID: " + floorId));

        Floorplan floorplan = new Floorplan();

        floorplan.setName(name);
        floorplan.setContentType(file.getContentType());
        floorplan.setData(file.getBytes());
        floorplan.setFloor(floor);

        return floorplanRepository.save(floorplan);
    }

    /* ---------- GET ALL FLOORPLANS ---------- */

    public List<Floorplan> getAllFloorplans() {
        return floorplanRepository.findAll();
    }

    /* ---------- GET FLOORPLAN BY ID ---------- */

    public Floorplan getFloorplan(Long id) {

        return floorplanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Floorplan not found with id: " + id));

    }

}