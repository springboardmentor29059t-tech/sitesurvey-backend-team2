package com.survey.site.service;

import com.survey.site.dto.CoordinateRequest;
import com.survey.site.model.*;
import com.survey.site.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SpaceCoordinateService {

    @Autowired
    private SpaceCoordinateRepository coordinateRepo;

    @Autowired
    private SpaceRepository spaceRepo;

    @Autowired
    private FloorplanRepository floorplanRepo;


    public SpaceCoordinate save(CoordinateRequest req) {

        Space space = spaceRepo.findById(req.getSpaceId())
                .orElseThrow(() -> new RuntimeException("Space not found"));

        Floorplan floorplan = floorplanRepo.findById(req.getFloorplanId())
                .orElseThrow(() -> new RuntimeException("Floorplan not found"));

        Optional<SpaceCoordinate> existing =
                coordinateRepo.findBySpaceIdAndFloorplanId(
                        req.getSpaceId(),
                        req.getFloorplanId()
                );

        SpaceCoordinate coord;

        if (existing.isPresent()) {
            coord = existing.get();
        } else {
            coord = new SpaceCoordinate();
            coord.setSpace(space);
            coord.setFloorplan(floorplan);
        }

        coord.setX(req.getX());
        coord.setY(req.getY());
        coord.setLabelName(req.getLabelName());
        coord.setType(req.getType());
        coord.setColor(req.getColor());
        coord.setNotes(req.getNotes());

        return coordinateRepo.save(coord);
    }


    public List<SpaceCoordinate> getByFloorplan(Long floorplanId) {
        return coordinateRepo.findByFloorplanId(floorplanId);
    }


    public void delete(Long id) {
        coordinateRepo.deleteById(id);
    }

    public SpaceCoordinate updatePosition(Long id, double x, double y) {
        SpaceCoordinate coord = coordinateRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Coordinate not found"));

        coord.setX(x);
        coord.setY(y);

        return coordinateRepo.save(coord);
    }

    public SpaceCoordinate saveCoordinate(Long spaceId, Long floorplanId,
                                          double x, double y,
                                          String labelName,
                                          String notes,
                                          String color) {

        Space space = spaceRepo.findById(spaceId).orElseThrow();
        Floorplan floorplan = floorplanRepo.findById(floorplanId).orElseThrow();

        SpaceCoordinate coord = new SpaceCoordinate();

        coord.setSpace(space);
        coord.setFloorplan(floorplan);
        coord.setX(x);
        coord.setY(y);

        coord.setLabelName(labelName);
        coord.setNotes(notes);
        coord.setColor(color);

        return coordinateRepo.save(coord);
    }
    public SpaceCoordinate getById(Long id) {
        return coordinateRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
    }

    public SpaceCoordinate save(SpaceCoordinate coord) {
        return coordinateRepo.save(coord);
    }
}