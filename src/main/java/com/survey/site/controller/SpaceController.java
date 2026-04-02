
package com.survey.site.controller;

import com.survey.site.model.Floor;
import com.survey.site.model.Space;
import com.survey.site.repository.FloorRepository;
import com.survey.site.repository.SpaceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/spaces")
@CrossOrigin("*")
public class SpaceController {

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private FloorRepository floorRepository;

    // ✅ CREATE SPACE (FIXED)
    @PostMapping
    public Space createSpace(@RequestBody Space space) {

        if (space.getFloor() == null || space.getFloor().getId() == null) {
            throw new RuntimeException("Floor ID missing");
        }

        Floor floor = floorRepository.findById(space.getFloor().getId())
                .orElseThrow(() -> new RuntimeException("Floor not found"));

        space.setFloor(floor);

        return spaceRepository.save(space);
    }

    // ✅ GET ALL
    @GetMapping
    public List<Space> getAllSpaces(){
        return spaceRepository.findAll();
    }

    // ✅ GET BY FLOOR
    @GetMapping("/floor/{floorId}")
    public List<Space> getByFloor(@PathVariable Long floorId){
        return spaceRepository.findByFloorId(floorId);
    }
}