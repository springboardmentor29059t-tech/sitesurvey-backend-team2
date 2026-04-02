package com.survey.site.controller;

import com.survey.site.dto.CoordinateRequest;
import com.survey.site.model.SpaceCoordinate;
import com.survey.site.service.SpaceCoordinateService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coordinates")
@CrossOrigin
public class SpaceCoordinateController {

    @Autowired
    private SpaceCoordinateService service;


//    @PostMapping
//    public SpaceCoordinate save(@RequestBody CoordinateRequest req) {
//        return service.save(req);
//    }
    @PostMapping
    public SpaceCoordinate save(@RequestBody Map<String, Object> body) {

        Long spaceId = Long.parseLong(body.get("spaceId").toString());
        Long floorplanId = Long.parseLong(body.get("floorplanId").toString());

        double x = Double.parseDouble(body.get("x").toString());
        double y = Double.parseDouble(body.get("y").toString());

        String labelName = (String) body.get("labelName");
        String notes = (String) body.get("notes");
        String color = (String) body.get("color");

        return service.saveCoordinate(spaceId, floorplanId, x, y, labelName, notes, color);
    }

    //  GET ALL
    @GetMapping("/{floorplanId}")
    public List<SpaceCoordinate> getAll(@PathVariable Long floorplanId) {
        return service.getByFloorplan(floorplanId);
    }

    //  DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // UPDATE POSITION (drag)
    @PutMapping("/{id}/position")
    public SpaceCoordinate updatePosition(
            @PathVariable Long id,
            @RequestBody CoordinateRequest req
    ) {
        return service.updatePosition(id, req.getX(), req.getY());
    }

    @PutMapping("/{id}")
    public SpaceCoordinate update(@PathVariable Long id,
                                  @RequestBody Map<String, Object> body) {

        SpaceCoordinate coord = service.getById(id);

        coord.setX(Double.parseDouble(body.get("x").toString()));
        coord.setY(Double.parseDouble(body.get("y").toString()));

        coord.setLabelName((String) body.get("labelName"));
        coord.setNotes((String) body.get("notes"));
        coord.setColor((String) body.get("color"));

        return service.save(coord);
    }
}