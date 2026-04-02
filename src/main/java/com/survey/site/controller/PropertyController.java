

package com.survey.site.controller;

import com.survey.site.model.Property;
import com.survey.site.model.User;
import com.survey.site.repository.PropertyRepository;
import com.survey.site.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/property")
@CrossOrigin
public class PropertyController {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserRepository userRepository;   // ✅ use User

    // Create Property
    @PostMapping("/add")
    public Property addProperty(@RequestBody Property property) {
        return propertyRepository.save(property);
    }

    // Assign Engineer to Property
    @PutMapping("/assign/{propertyId}/{engineerId}")
    public Property assignEngineer(
            @PathVariable Long propertyId,
            @PathVariable Long engineerId) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        User engineer = userRepository.findById(engineerId)
                .orElseThrow(() -> new RuntimeException("Engineer not found"));

        // ✅ safety check
        if (!engineer.getRole().equals("ENGINEER")) {
            throw new RuntimeException("User is not an Engineer");
        }

        property.setEngineer(engineer);

        return propertyRepository.save(property);
    }

    // Get properties assigned to engineer
    @GetMapping("/engineer/{engineerId}")
    public List<Property> getEngineerProperties(@PathVariable Long engineerId) {
        return propertyRepository.findByEngineerId(engineerId);
    }

    // Get all properties
    @GetMapping("/all")
    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }
}