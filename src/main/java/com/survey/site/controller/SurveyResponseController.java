
package com.survey.site.controller;

import com.survey.site.model.SurveyResponse;
import com.survey.site.service.SurveyResponseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/survey")
@CrossOrigin("*")
public class SurveyResponseController {

    @Autowired
    private SurveyResponseService surveyService;


    @PostMapping("/submit")
    public List<SurveyResponse> submit(@RequestBody List<SurveyResponse> responses) {
        return surveyService.saveResponses(responses);
    }


    @GetMapping("/space/{spaceId}")
    public List<SurveyResponse> getBySpace(@PathVariable Long spaceId) {
        return surveyService.getBySpace(spaceId);
    }


    @GetMapping("/all")
    public List<SurveyResponse> getAll() {
        return surveyService.getAll();
    }
}