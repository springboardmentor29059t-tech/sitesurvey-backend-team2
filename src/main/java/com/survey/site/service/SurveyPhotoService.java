


package com.survey.site.service;

import com.survey.site.model.Survey;
import com.survey.site.model.SurveyPhoto;
import com.survey.site.repository.SurveyPhotoRepository;
import com.survey.site.repository.SurveyRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class SurveyPhotoService {

    @Autowired
    private SurveyPhotoRepository surveyPhotoRepository;

    @Autowired
    private SurveyRepository surveyRepository;

    // ✅ UPLOAD PHOTO USING surveyId (NOT spaceId)
    public SurveyPhoto uploadPhoto(Long surveyId, MultipartFile file) throws IOException {

        Survey survey = surveyRepository.findById(surveyId).orElseThrow();

        SurveyPhoto photo = new SurveyPhoto();
        photo.setContentType(file.getContentType());
        photo.setData(file.getBytes());

        // ✅ IMPORTANT RELATION
        photo.setSurvey(survey);

        return surveyPhotoRepository.save(photo);
    }

    // ✅ GET PHOTOS BY SURVEY
    public List<SurveyPhoto> getPhotosBySurvey(Long surveyId) {
        return surveyPhotoRepository.findBySurveyId(surveyId);
    }

    // ✅ DOWNLOAD PHOTO
    public byte[] downloadPhoto(Long id) {
        SurveyPhoto photo = surveyPhotoRepository.findById(id).orElseThrow();
        return photo.getData();
    }
}