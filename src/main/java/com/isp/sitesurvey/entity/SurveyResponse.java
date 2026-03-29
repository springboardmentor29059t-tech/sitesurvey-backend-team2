package com.isp.sitesurvey.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "survey_responses")
public class SurveyResponse {

    public enum Status { DRAFT, SUBMITTED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    // Supports both persisted IDs and "temp-xxxxx" canvas IDs
    @Column(name = "equipment_id")
    private String equipmentId;

    @Column(name = "engineer_id", nullable = false)
    private Long engineerId;

    // DRAFT = autosaved but not finalised; SUBMITTED = locked
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.DRAFT;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "last_saved_at")
    private LocalDateTime lastSavedAt;

    @Column(name = "checklist_template_id")
    private Long checklistTemplateId;

    @JsonManagedReference
    @OneToMany(mappedBy = "surveyResponse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyResponseItem> answers = new ArrayList<>();

    @PrePersist
    @PreUpdate
    protected void onSave() {
        lastSavedAt = LocalDateTime.now();
        if (status == Status.SUBMITTED && submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }

    public SurveyResponse() {}

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPropertyId() { return propertyId; }
    public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }
    public String getEquipmentId() { return equipmentId; }
    public void setEquipmentId(String equipmentId) { this.equipmentId = equipmentId; }
    public Long getEngineerId() { return engineerId; }
    public void setEngineerId(Long engineerId) { this.engineerId = engineerId; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime t) { this.submittedAt = t; }
    public LocalDateTime getLastSavedAt() { return lastSavedAt; }
    public void setLastSavedAt(LocalDateTime t) { this.lastSavedAt = t; }
    public Long getChecklistTemplateId() { return checklistTemplateId; }
    public void setChecklistTemplateId(Long id) { this.checklistTemplateId = id; }
    public List<SurveyResponseItem> getAnswers() { return answers; }
    public void setAnswers(List<SurveyResponseItem> answers) { this.answers = answers; }
}