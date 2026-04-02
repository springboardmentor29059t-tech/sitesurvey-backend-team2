

package com.survey.site.controller;

import com.survey.site.model.SurveyPhoto;
import com.survey.site.service.SurveyPhotoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/photos")
@CrossOrigin("*")
public class SurveyPhotoController {

    @Autowired
    private SurveyPhotoService surveyPhotoService;

    // UPLOAD PHOTO (USE surveyId)
    @PostMapping("/survey/{surveyId}")
    public SurveyPhoto uploadPhoto(
            @PathVariable Long surveyId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        return surveyPhotoService.uploadPhoto(surveyId, file);
    }

    //  GET PHOTOS BY SURVEY
    @GetMapping("/survey/{surveyId}")
    public List<SurveyPhoto> getPhotos(@PathVariable Long surveyId) {

        return surveyPhotoService.getPhotosBySurvey(surveyId);
    }
}