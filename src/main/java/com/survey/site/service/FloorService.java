

package com.survey.site.service;

import com.survey.site.model.Building;
import com.survey.site.model.Floor;
import com.survey.site.repository.BuildingRepository;
import com.survey.site.repository.FloorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FloorService {

    @Autowired
    private FloorRepository floorRepository;

    @Autowired
    private BuildingRepository buildingRepository;

    // Create Floor
    public Floor createFloor(Floor floor) {
        // Fetch building from DB to avoid incomplete data
        Long buildingId = floor.getBuilding().getId();
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new RuntimeException("Building not found with id: " + buildingId));
        floor.setBuilding(building);

        return floorRepository.save(floor);
    }

    // Get all floors
    public List<Floor> getAllFloors() {
        return floorRepository.findAll();
    }

    // Get floor by id
    public Floor getFloorById(Long id) {
        return floorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Floor not found with id: " + id));
    }

    // Update Floor
    public Floor updateFloor(Long id, Floor floorDetails) {
        Floor existingFloor = floorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Floor not found with id: " + id));

        existingFloor.setName(floorDetails.getName());
        existingFloor.setFloorNumber(floorDetails.getFloorNumber());

        // Update building if provided
        if (floorDetails.getBuilding() != null && floorDetails.getBuilding().getId() != null) {
            Building building = buildingRepository.findById(floorDetails.getBuilding().getId())
                    .orElseThrow(() -> new RuntimeException("Building not found with id: " + floorDetails.getBuilding().getId()));
            existingFloor.setBuilding(building);
        }

        return floorRepository.save(existingFloor);
    }

    // Delete floor
    public void deleteFloor(Long id) {
        Floor floor = floorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Floor not found with id: " + id));
        floorRepository.delete(floor);
    }
    public List<Floor> getFloorsByProperty(Long propertyId) {
        return floorRepository.findByBuildingPropertyId(propertyId);
    }
    public List<Floor> getFloorsByBuilding(Long buildingId) {
        return floorRepository.findByBuildingId(buildingId);
    }

}