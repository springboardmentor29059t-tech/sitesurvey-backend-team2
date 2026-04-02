

package com.survey.site.service;

import com.survey.site.model.*;
import com.survey.site.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class SurveyService {

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SurveyPhotoRepository photoRepo;

    @Autowired
    private SpaceRepository spaceRepository;




    public Survey saveSurvey(Long spaceId, Long engineerId, Survey survey) {


        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new RuntimeException("Space not found: " + spaceId));


        User engineer = userRepository.findById(engineerId)
                .orElseThrow(() -> new RuntimeException("Engineer not found: " + engineerId));


        survey.setSpace(space);
        survey.setEngineer(engineer);

        return surveyRepository.save(survey);
    }

    public SurveyPhoto uploadPhoto(Long surveyId, MultipartFile file) throws IOException {

        Survey survey = surveyRepository.findById(surveyId).orElseThrow();

        SurveyPhoto photo = new SurveyPhoto();
        photo.setContentType(file.getContentType());
        photo.setData(file.getBytes());
        photo.setSurvey(survey);

        return photoRepo.save(photo);
    }
    public Survey createSurvey(Survey survey){
       survey.setStatus("Draft");
       return surveyRepository.save(survey);
    }
 public List<Survey> getSurveysBySpace(Long spaceId){
        return surveyRepository.findBySpaceId(spaceId);
    }

    public Survey submitSurvey(Long surveyId){

        Survey survey = surveyRepository.findById(surveyId).orElseThrow();

        survey.setStatus("Submitted");

        return surveyRepository.save(survey);
    }

public Survey saveSurvey(Map<String, Object> body) {

    Survey survey = new Survey();

    survey.setChecklist((String) body.get("checklist"));
    survey.setRemarks((String) body.get("remarks"));
    survey.setNetworkType((String) body.get("networkType"));
    survey.setSignalValue((String) body.get("signalValue"));


    survey.setStatus("COMPLETED");   // or "SUBMITTED"



    return surveyRepository.save(survey);
}
}