package com.survey.site.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;

    private String text;
    private String category;
    private String responseType;
    private String allowedValues;
    private boolean mandatory;

    // ✅ ADD THIS
    @ManyToOne
    @JoinColumn(name = "template_id")
    @JsonIgnoreProperties("questions")
    private ChecklistTemplate template;

    // ================= GETTERS =================
    public Long getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public String getText() {
        return text;
    }

    public String getCategory() {
        return category;
    }

    public String getResponseType() {
        return responseType;
    }

    public String getAllowedValues() {
        return allowedValues;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public ChecklistTemplate getTemplate() {
        return template;
    }

    // ================= SETTERS =================
    public void setId(Long id) {
        this.id = id;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public void setAllowedValues(String allowedValues) {
        this.allowedValues = allowedValues;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    // ✅ THIS FIXES YOUR ERROR
    public void setTemplate(ChecklistTemplate template) {
        this.template = template;
    }
}