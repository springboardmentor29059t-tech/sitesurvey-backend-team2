package com.isp.sitesurvey.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "survey_response_items")
public class SurveyResponseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "checklist_item_id", nullable = false)
    private Long checklistItemId;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    // Stored filename in uploads/ directory (set by attachment endpoint)
    @Column(name = "photo_url")
    private String photoUrl;

    // Original filename for display purposes
    @Column(name = "photo_filename")
    private String photoFilename;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_response_id", nullable = false)
    private SurveyResponse surveyResponse;

    public SurveyResponseItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getChecklistItemId() { return checklistItemId; }
    public void setChecklistItemId(Long id) { this.checklistItemId = id; }
    public String getAnswerText() { return answerText; }
    public void setAnswerText(String text) { this.answerText = text; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String url) { this.photoUrl = url; }
    public String getPhotoFilename() { return photoFilename; }
    public void setPhotoFilename(String name) { this.photoFilename = name; }
    public SurveyResponse getSurveyResponse() { return surveyResponse; }
    public void setSurveyResponse(SurveyResponse r) { this.surveyResponse = r; }
}