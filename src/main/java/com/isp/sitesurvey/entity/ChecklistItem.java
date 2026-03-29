package com.isp.sitesurvey.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "checklist_items")
public class ChecklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String question;

    @Column(name = "response_type", nullable = false)
    private String responseType;

    @Column(name = "is_required")
    private Boolean isRequired = false;

    // ✅ FIX 1: Use @JsonIgnore instead of @JsonBackReference.
    // @JsonBackReference was silently dropping the entire items list
    // in the parent when serialized in certain Spring Boot versions.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ChecklistTemplate template;

    public ChecklistItem() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    // ✅ FIX 2: Add questionText alias so the React frontend's
    // item.questionText works without changing the DB column name.
    public String getQuestionText() { return question; }

    public String getResponseType() { return responseType; }
    public void setResponseType(String responseType) { this.responseType = responseType; }
    public Boolean getIsRequired() { return isRequired; }
    public void setIsRequired(Boolean isRequired) { this.isRequired = isRequired; }
    public ChecklistTemplate getTemplate() { return template; }
    public void setTemplate(ChecklistTemplate template) { this.template = template; }
}