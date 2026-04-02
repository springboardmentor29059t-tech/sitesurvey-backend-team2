package com.survey.site.controller;

import com.survey.site.model.EngineerAssignment;
import com.survey.site.repository.EngineerAssignmentRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assignments")
@CrossOrigin("*")
public class EngineerAssignmentController {

    private final EngineerAssignmentRepository repo;

    public EngineerAssignmentController(EngineerAssignmentRepository repo){
        this.repo = repo;
    }

    // Assign task
    @PostMapping
    public EngineerAssignment assign(@RequestBody EngineerAssignment assignment){

        assignment.setStatus("Pending");
        return repo.save(assignment);
    }

    // Engineer dashboard tasks
    @GetMapping("/engineer/{engineerId}")
    public List<EngineerAssignment> getEngineerTasks(
            @PathVariable Long engineerId){

        return repo.findByEngineerId(engineerId);
    }
}