package com.isp.sitesurvey.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "checklist_responses")
public class ChecklistResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne
    @JoinColumn(name = "survey_response_id")
    private SurveyResponse surveyResponse;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private ChecklistQuestion question;

    private String answerValue;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SurveyResponse getSurveyResponse() { return surveyResponse; }
    public void setSurveyResponse(SurveyResponse surveyResponse) { this.surveyResponse = surveyResponse; }
    public ChecklistQuestion getQuestion() { return question; }
    public void setQuestion(ChecklistQuestion question) { this.question = question; }
    public String getAnswerValue() { return answerValue; }
    public void setAnswerValue(String answerValue) { this.answerValue = answerValue; }
}
