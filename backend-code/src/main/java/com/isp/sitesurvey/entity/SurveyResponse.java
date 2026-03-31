package com.isp.sitesurvey.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "survey_responses")
public class SurveyResponse {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false)
    private Space space;
    
    @Column(name = "power_available")
    private Boolean powerAvailable;
    
    @Column(name = "signal_strength_dbm")
    private Integer signalStrengthDbm;
    
    @Column(name = "cooling_available")
    private Boolean coolingAvailable;
    
    @Column(columnDefinition = "TEXT")
    private String obstacles;
    
    @Column(columnDefinition = "TEXT")
    private String additionalNotes;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "surveyor_id")
    private Long surveyorId;

    @OneToMany(mappedBy = "surveyResponse", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ChecklistResponse> checklistResponses = new ArrayList<>();

    @JsonIgnore
    @Lob
    @Column(name = "photo_data", columnDefinition = "LONGBLOB")
    private byte[] photoData;

    @Column(name = "photo_content_type")
    private String photoContentType;

    @Column(name = "status", length = 20)
    private String status = "SUBMITTED"; // Default to SUBMITTED for backward compatibility

    @PrePersist
    protected void onCreate() {
        completedAt = LocalDateTime.now();
    }

    // --- GETTERS & SETTERS ---
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Space getSpace() { return space; }
    public void setSpace(Space space) { this.space = space; }

    public Boolean getPowerAvailable() { return powerAvailable; }
    public void setPowerAvailable(Boolean powerAvailable) { this.powerAvailable = powerAvailable; }

    public Integer getSignalStrengthDbm() { return signalStrengthDbm; }
    public void setSignalStrengthDbm(Integer signalStrengthDbm) { this.signalStrengthDbm = signalStrengthDbm; }

    public Boolean getCoolingAvailable() { return coolingAvailable; }
    public void setCoolingAvailable(Boolean coolingAvailable) { this.coolingAvailable = coolingAvailable; }

    public String getObstacles() { return obstacles; }
    public void setObstacles(String obstacles) { this.obstacles = obstacles; }

    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Long getSurveyorId() { return surveyorId; }
    public void setSurveyorId(Long surveyorId) { this.surveyorId = surveyorId; }

    public List<ChecklistResponse> getChecklistResponses() { return checklistResponses; }
    public void setChecklistResponses(List<ChecklistResponse> checklistResponses) { this.checklistResponses = checklistResponses; }

    public byte[] getPhotoData() { return photoData; }
    public void setPhotoData(byte[] photoData) { this.photoData = photoData; }

    public String getPhotoContentType() { return photoContentType; }
    public void setPhotoContentType(String photoContentType) { this.photoContentType = photoContentType; }
}
