
package com.survey.site.service;

import com.survey.site.controller.BuildingController;
import com.survey.site.model.Building;
import com.survey.site.model.Property;
import com.survey.site.repository.BuildingRepository;
import com.survey.site.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuildingService {

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private PropertyRepository propertyRepository;


    public Building createBuilding(BuildingController.BuildingRequest request){
        Property property = propertyRepository.findById(request.propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + request.propertyId));

        Building building = new Building();
        building.setName(request.name);
        building.setProperty(property);

        return buildingRepository.save(building);
    }

    public List<Building> getAllBuildings(){
        return buildingRepository.findAll();
    }

    public String deleteBuilding(Long id){
        buildingRepository.deleteById(id);
        return "Building deleted";
    }
}