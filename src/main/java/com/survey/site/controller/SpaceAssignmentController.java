package com.survey.site.controller;

import com.survey.site.model.Engineer;
import com.survey.site.model.Space;
import com.survey.site.repository.EngineerRepository;
import com.survey.site.repository.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/space-assignment")
@CrossOrigin("*")
public class SpaceAssignmentController {

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private EngineerRepository engineerRepository;

    @PutMapping("/{spaceId}/assign/{engineerId}")
    public Space assignEngineer(@PathVariable Long spaceId,
                                @PathVariable Long engineerId) {

        Space space = spaceRepository.findById(spaceId).orElse(null);
        Engineer engineer = engineerRepository.findById(engineerId).orElse(null);

        space.setEngineer(engineer);

        return spaceRepository.save(space);
    }
}