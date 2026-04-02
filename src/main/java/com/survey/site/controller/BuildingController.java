

package com.survey.site.controller;

import com.survey.site.model.Building;
import com.survey.site.repository.BuildingRepository;
import com.survey.site.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/building")
@CrossOrigin(origins = "http://localhost:5173") // React dev URL
public class BuildingController {

    @Autowired
    private BuildingService buildingService;

    @Autowired
    private BuildingRepository buildingRepository;


    // ✅ Create Building
    @PostMapping("/add")
    public Building createBuilding(@RequestBody BuildingRequest request){
        return buildingService.createBuilding(request);
    }

    @GetMapping("/all")
    public List<Building> getAllBuildings(){
        return buildingService.getAllBuildings();
    }

    @DeleteMapping("/{id}")
    public String deleteBuilding(@PathVariable Long id){
        return buildingService.deleteBuilding(id);
    }

    // DTO to receive JSON
    public static class BuildingRequest {
        public String name;
        public Long propertyId;
    }

    @GetMapping("/property/{propertyId}")
    public List<Building> getBuildings(@PathVariable Long propertyId) {
        return buildingRepository.findByPropertyId(propertyId);
    }


}

