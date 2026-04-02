
package com.survey.site.service;

import com.survey.site.model.*;
import com.survey.site.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SurveyResponseService {

    @Autowired
    private SurveyResponseRepository surveyRepo;

    @Autowired
    private SpaceRepository spaceRepo;

    @Autowired
    private QuestionRepository questionRepo;


    public List<SurveyResponse> saveResponses(List<SurveyResponse> responses) {

        for (SurveyResponse r : responses) {

            // 🚨 VALIDATION
            if (r.getSpace() == null || r.getSpace().getId() == null) {
                throw new RuntimeException("Space ID missing in request");
            }

            if (r.getQuestion() == null || r.getQuestion().getId() == null) {
                throw new RuntimeException("Question ID missing in request");
            }


            Space space = spaceRepo.findById(r.getSpace().getId())
                    .orElseThrow(() -> new RuntimeException("Space not found"));

            Question question = questionRepo.findById(r.getQuestion().getId())
                    .orElseThrow(() -> new RuntimeException("Question not found"));


            r.setSpace(space);
            r.setQuestion(question);
        }

        return surveyRepo.saveAll(responses);
    }


    public List<SurveyResponse> getBySpace(Long spaceId) {
        return surveyRepo.findBySpaceId(spaceId);
    }


    public List<SurveyResponse> getAll() {
        return surveyRepo.findAll();
    }
}