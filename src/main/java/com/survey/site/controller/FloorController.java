

package com.survey.site.controller;

import com.survey.site.model.Floor;
import com.survey.site.service.FloorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/floors")
public class FloorController {

    @Autowired
    private FloorService floorService;

    // Create a new floor
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORG_ADMIN')")
    @PostMapping
    public Floor createFloor(@RequestBody Floor floor) {
        return floorService.createFloor(floor);
    }

    // Get all floors
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORG_ADMIN')")
    @GetMapping
    public List<Floor> getAllFloors() {
        return floorService.getAllFloors();
    }

    // Get floor by id
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORG_ADMIN')")
    @GetMapping("/{id}")
    public Floor getFloorById(@PathVariable Long id) {
        return floorService.getFloorById(id);
    }

    // Update a floor
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORG_ADMIN')")
    @PutMapping("/{id}")
    public Floor updateFloor(@PathVariable Long id, @RequestBody Floor floor) {
        return floorService.updateFloor(id, floor);
    }

    // Delete a floor
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORG_ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteFloor(@PathVariable Long id) {
        floorService.deleteFloor(id);
        return "Floor deleted successfully";
    }
    @GetMapping("/property/{propertyId}")
    public List<Floor> getFloorsByProperty(@PathVariable Long propertyId) {
        return floorService.getFloorsByProperty(propertyId);
    }

    @GetMapping("/building/{buildingId}")
    public List<Floor> getFloors(@PathVariable Long buildingId) {
        return floorService.getFloorsByBuilding(buildingId);
    }
}