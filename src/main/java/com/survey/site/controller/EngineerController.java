


package com.survey.site.controller;

import com.survey.site.model.Floorplan;
import com.survey.site.model.Property;
import com.survey.site.model.PropertyImage;
import com.survey.site.service.EngineerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/engineer")
@CrossOrigin
public class EngineerController {

    private final EngineerService engineerService;

    public EngineerController(EngineerService engineerService) {
        this.engineerService = engineerService;
    }

    // ----------------- PHASE 1 -----------------
    @GetMapping("/my-properties")
    public List<Property> getMyProperties(Authentication auth) {
        return engineerService.getMyProperties(auth);
    }

    @GetMapping("/dashboard")
    public long getDashboard(Authentication auth) {
        return engineerService.getDashboardCount(auth);
    }

    @GetMapping("/property/{id}")
    public Property getProperty(@PathVariable Long id, Authentication auth) {
        return engineerService.getProperty(id, auth);
    }

    // ----------------- PHASE 2 -----------------
    @PutMapping("/property/{id}/status")
    public Property updateStatus(@PathVariable Long id,
                                 @RequestParam String status,
                                 Authentication auth) {
        return engineerService.updateStatus(id, status, auth);
    }

    @PutMapping("/property/{id}/remarks")
    public Property addRemarks(@PathVariable Long id,
                               @RequestParam String remarks,
                               Authentication auth) {
        return engineerService.addRemarks(id, remarks, auth);
    }

    // ----------------- PHASE 3 -----------------
    @PostMapping("/property/{id}/upload")
    public PropertyImage uploadImage(@PathVariable Long id,
                                     @RequestParam("file") MultipartFile file,
                                     Authentication auth) throws IOException {
        return engineerService.uploadImage(id, file, auth);
    }

    @GetMapping("/property/{id}/images")
    public List<PropertyImage> getImages(@PathVariable Long id,
                                         Authentication auth) {
        return engineerService.getImages(id, auth);
    }

    @GetMapping("/image/{imageId}")
    public ResponseEntity<byte[]> viewImage(@PathVariable Long imageId) {
        PropertyImage image = engineerService.getImage(imageId);
        return ResponseEntity.ok()
                .header("Content-Type", image.getFileType())
                .body(image.getData());
    }

    @PutMapping("/property/{id}/submit")
    public Property submitReport(@PathVariable Long id,
                                 @RequestBody String report,
                                 Authentication auth) {
        return engineerService.submitReport(id, report, auth);
    }

    @GetMapping("/property/{id}/floorplans")
    public List<Floorplan> getFloorplans(@PathVariable Long id, Authentication auth) {
        return engineerService.getFloorplans(id, auth);
    }
}