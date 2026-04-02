



package com.survey.site.service;

import com.survey.site.model.Floorplan;
import com.survey.site.model.Property;
import com.survey.site.model.PropertyImage;
import com.survey.site.model.User;
import com.survey.site.repository.FloorplanRepository;
import com.survey.site.repository.PropertyRepository;
import com.survey.site.repository.PropertyImageRepository;
import com.survey.site.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Service
public class EngineerService {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private PropertyImageRepository imageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FloorplanRepository floorplanRepository;


    private User getUser(Authentication auth) {
        if (auth == null) throw new RuntimeException("No authentication found");
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Engineer not found"));
    }


    public List<Property> getMyProperties(Authentication auth) {
        User engineer = getUser(auth);
        return propertyRepository.findByEngineerId(engineer.getId());
    }

    public Long getDashboardCount(Authentication auth) {
        User engineer = getUser(auth);
        return propertyRepository.countByEngineerId(engineer.getId());
    }

    public Property getProperty(Long propertyId, Authentication auth) {
        User engineer = getUser(auth);
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        if (property.getEngineer() == null || !property.getEngineer().getId().equals(engineer.getId()))
            throw new RuntimeException("Access denied");

        return property;
    }


    public Property updateStatus(Long propertyId, String status, Authentication auth) {
        Property property = getProperty(propertyId, auth);
        property.setStatus(status);
        return propertyRepository.save(property);
    }

    public Property addRemarks(Long propertyId, String remarks, Authentication auth) {
        Property property = getProperty(propertyId, auth);
        String existingRemarks = property.getRemarks() != null ? property.getRemarks() : "";
        property.setRemarks(existingRemarks + "\n" + remarks);
        return propertyRepository.save(property);
    }


    public PropertyImage uploadImage(Long propertyId, MultipartFile file, Authentication auth) throws IOException {
        User engineer = getUser(auth);
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        if (property.getEngineer() == null || !property.getEngineer().getId().equals(engineer.getId()))
            throw new RuntimeException("Access denied");

        PropertyImage image = new PropertyImage();
        image.setFileName(file.getOriginalFilename());
        image.setFileType(file.getContentType());
        image.setData(file.getBytes());
        image.setProperty(property);

        return imageRepository.save(image);
    }

    public List<PropertyImage> getImages(Long propertyId, Authentication auth) {
        User engineer = getUser(auth);
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        if (property.getEngineer() == null || !property.getEngineer().getId().equals(engineer.getId()))
            throw new RuntimeException("Access denied");

        return imageRepository.findByPropertyId(propertyId);
    }

    public PropertyImage getImage(Long imageId) {
        return imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));
    }

    public Property submitReport(Long propertyId, String report, Authentication auth) {
        User engineer = getUser(auth);
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        if (property.getEngineer() == null || !property.getEngineer().getId().equals(engineer.getId()))
            throw new RuntimeException("Access denied");

        property.setFinalReport(report);
        property.setSubmitted(true);
        property.setStatus("COMPLETED");

        return propertyRepository.save(property);
    }

//    public List<Floorplan> getFloorplans(Long propertyId, Authentication auth) {
//        User engineer = getUser(auth);
//        Property property = propertyRepository.findById(propertyId)
//                .orElseThrow(() -> new RuntimeException("Property not found"));
//
//        if (property.getEngineer() == null || !property.getEngineer().getId().equals(engineer.getId())) {
//            throw new RuntimeException("Access denied");
//        }
//
//        return floorplanRepository.findByPropertyId(propertyId);
//    }

    public List<Floorplan> getFloorplans(Long propertyId, Authentication auth) {
        User engineer = getUser(auth);
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        if (property.getEngineer() == null || !property.getEngineer().getId().equals(engineer.getId())) {
            throw new RuntimeException("Access denied");
        }


        return floorplanRepository.findByFloor_Building_Property_Id(propertyId);
    }
}