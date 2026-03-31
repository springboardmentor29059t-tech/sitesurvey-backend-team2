package com.isp.sitesurvey.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "checklist_questions")
public class ChecklistQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private ChecklistTemplate template;

    private String questionText;
    private String type; // BOOLEAN, STRING, INTEGER

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ChecklistTemplate getTemplate() { return template; }
    public void setTemplate(ChecklistTemplate template) { this.template = template; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
