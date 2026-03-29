package com.isp.sitesurvey.service;

import com.isp.sitesurvey.entity.Property;
import com.isp.sitesurvey.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyService {

    private final PropertyRepository propertyRepository;

    @Transactional
    public Property createProperty(Property property) {
        log.info("Creating new property: {}", property.getName());
        return propertyRepository.save(property);
    }

    @Transactional(readOnly = true)
    public Property getPropertyById(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }

    @Transactional
    public Property updateProperty(Long id, Property updatedProperty) {
        Property existingProperty = getPropertyById(id);
        
        // Update core fields
        existingProperty.setName(updatedProperty.getName());
        
        // If your property has other fields like address, city, etc., 
        // they can be updated here like this:
        // existingProperty.setAddressLine1(updatedProperty.getAddressLine1());
        // existingProperty.setCity(updatedProperty.getCity());
        
        log.info("Updating property: {}", id);
        return propertyRepository.save(existingProperty);
    }

    @Transactional
    public void deleteProperty(Long id) {
        log.info("Deleting property: {}", id);
        propertyRepository.deleteById(id);
    }
}