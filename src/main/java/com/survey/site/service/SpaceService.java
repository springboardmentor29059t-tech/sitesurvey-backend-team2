
package com.survey.site.service;

import com.survey.site.model.Floor;
import com.survey.site.model.Space;
import com.survey.site.repository.FloorRepository;
import com.survey.site.repository.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpaceService {

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private FloorRepository floorRepository;

    public Space createSpace(Space space){

        Long floorId = space.getFloor().getId();

        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> new RuntimeException("Floor not found"));

        space.setFloor(floor);

        return spaceRepository.save(space);
    }

}