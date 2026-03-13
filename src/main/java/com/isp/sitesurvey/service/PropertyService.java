package com.isp.sitesurvey.service;

import com.isp.sitesurvey.entity.Organization;
import com.isp.sitesurvey.entity.Property;
import com.isp.sitesurvey.repository.OrganizationRepository;
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
    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Property getPropertyById(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found: " + id));
    }

    @Transactional
    public Property createProperty(Property property) {
        // Get default organization (or create if not exists)
        Organization org = organizationRepository.findByName("Default ISP Organization")
                .orElseGet(() -> {
                    Organization newOrg = new Organization();
                    newOrg.setName("Default ISP Organization");
                    return organizationRepository.save(newOrg);
                });

        property.setOrganization(org);
        
        log.info("Creating property: {}", property.getName());
        return propertyRepository.save(property);
    }

    @Transactional
    public Property updateProperty(Long id, Property propertyDetails) {
        Property property = getPropertyById(id);
        
        property.setName(propertyDetails.getName());
        property.setAddressLine1(propertyDetails.getAddressLine1());
        property.setAddressLine2(propertyDetails.getAddressLine2());
        property.setCity(propertyDetails.getCity());
        property.setState(propertyDetails.getState());
        property.setPostalCode(propertyDetails.getPostalCode());
        property.setCountry(propertyDetails.getCountry());
        
        return propertyRepository.save(property);
    }

    @Transactional
    public void deleteProperty(Long id) {
        propertyRepository.deleteById(id);
        log.info("Deleted property: {}", id);
    }
}