package com.survey.site.service;

import com.survey.site.model.Property;
import com.survey.site.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertyService {

    @Autowired
    private PropertyRepository propertyRepository;

    // Create property
    public Property createProperty(Property property) {
        return propertyRepository.save(property);
    }

    // Get all properties
    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }

    // Delete property
    public String deleteProperty(Long id) {
        propertyRepository.deleteById(id);
        return "Property deleted successfully";
    }
}