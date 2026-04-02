package com.survey.site.controller;

import com.survey.site.model.User;
import com.survey.site.model.PropertyImage;
import com.survey.site.service.OrgAdminService;
import com.survey.site.service.EngineerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orgadmin")
@CrossOrigin("*")
public class OrgAdminController {

    @Autowired
    private OrgAdminService orgAdminService;

    @Autowired
    private EngineerService engineerService;

    // ✅ CREATE ENGINEER
    @PostMapping("/create-engineer")
    public User createEngineer(@RequestBody User user) {
        return orgAdminService.createEngineer(user);
    }

    // ✅ VIEW ENGINEERS
    @GetMapping("/engineers")
    public List<User> getEngineers() {
        return orgAdminService.getEngineers();
    }

    // ✅ DELETE ENGINEER
    @DeleteMapping("/engineer/{id}")
    public String deleteEngineer(@PathVariable Long id) {
        return orgAdminService.deleteEngineer(id);
    }

    // ✅ GET IMAGES OF PROPERTY (NEW)
    @GetMapping("/property/{id}/images")
    public List<PropertyImage> getPropertyImages(@PathVariable Long id) {
        return engineerService.getImages(id, null);
    }

    // ✅ VIEW IMAGE (NEW)
    @GetMapping("/image/{imageId}")
    public ResponseEntity<byte[]> viewImage(@PathVariable Long imageId) {

        PropertyImage image = engineerService.getImage(imageId);

        return ResponseEntity.ok()
                .header("Content-Type", image.getFileType())
                .body(image.getData());
    }
}