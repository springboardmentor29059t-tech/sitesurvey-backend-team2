

package com.survey.site.controller;

import com.survey.site.model.*;
import com.survey.site.repository.SpaceRepository;
import com.survey.site.repository.SurveyPhotoRepository;
import com.survey.site.repository.SurveyRepository;
import com.survey.site.service.SurveyService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class SurveyController {

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private SurveyService surveyService;

    @Autowired
   private SurveyRepository surveyRepository;

    @Autowired
    private  SurveyPhotoRepository surveyPhotoRepository;


    @GetMapping("/property/{propertyId}/spaces")
    public List<Space> getSpaces(@PathVariable Long propertyId) {
        return spaceRepository.findByFloorBuildingPropertyId(propertyId);
    }


//    @PostMapping("/survey")
//    public Survey saveSurvey(@RequestBody Map<String, Object> body) {
//
//        Long spaceId = Long.valueOf(body.get("spaceId").toString());
//        Long engineerId = Long.valueOf(body.get("engineerId").toString());
//
//        Survey survey = new Survey();
////        survey.setChecklist((String) body.get("checklist"));
//        survey.setRemarks((String) body.get("remarks"));
//        survey.setNetworkType((String) body.get("networkType"));
//        survey.setSignalValue((String) body.get("signalValue"));
//        survey.setStatus((String) body.get("status"));
//
//        return surveyService.saveSurvey(spaceId, engineerId, survey);
//    }
@PostMapping("/survey")
public Survey saveSurvey(@RequestBody Map<String, Object> body) {


    System.out.println("REQUEST BODY: " + body);


    String checklist = body.get("checklist") != null ? body.get("checklist").toString() : "";
    String remarks = body.get("remarks") != null ? body.get("remarks").toString() : "";
    String networkType = body.get("networkType") != null ? body.get("networkType").toString() : "";
    String signalValue = body.get("signalValue") != null ? body.get("signalValue").toString() : "";

    Long spaceId = body.get("spaceId") != null
            ? Long.parseLong(body.get("spaceId").toString())
            : null;

    Long engineerId = null;

    try {
        engineerId = Long.parseLong(body.get("engineerId").toString());
    } catch (Exception e) {
        throw new RuntimeException("Invalid engineerId: " + body.get("engineerId"));
    }
    if (spaceId == null || engineerId == null) {
        throw new RuntimeException("spaceId or engineerId missing");
    }


    Survey survey = new Survey();
    survey.setChecklist(checklist);
    survey.setRemarks(remarks);
    survey.setNetworkType(networkType);
    survey.setSignalValue(signalValue);
//    survey.setStatus("Draft");
    survey.setStatus(
            body.get("status") != null ? body.get("status").toString() : "Draft"
    );

    return surveyService.saveSurvey(spaceId, engineerId, survey);
}



    @PostMapping("/survey/{surveyId}/photo")
    public SurveyPhoto uploadPhoto(
            @PathVariable Long surveyId,
            @RequestParam("file") MultipartFile file) throws IOException {

        return surveyService.uploadPhoto(surveyId, file);
    }

    @PostMapping("/submit")
   public Survey submitSurvey(@RequestBody Survey survey) {

        survey.setStatus("COMPLETED");
        return surveyRepository.save(survey);

    }

    @GetMapping("/engineer/{engineerId}")
    public List<Survey> getSurveys(@PathVariable Long engineerId) {

        return surveyRepository.findByEngineerId(engineerId);
    }
    @GetMapping("/all")
    public List<Survey> getAll() {
        return surveyRepository.findAll();
    }

    @GetMapping("/survey/{surveyId}/photo")
    public ResponseEntity<?> getPhoto(@PathVariable Long surveyId) {

        List<SurveyPhoto> photos = surveyPhotoRepository.findBySurveyId(surveyId);

        if (photos.isEmpty()) {
            return ResponseEntity.ok("No Images");
        }


        SurveyPhoto photo = photos.get(0);

        return ResponseEntity.ok()
                .header("Content-Type", photo.getContentType())
                .body(photo.getData());
    }

}