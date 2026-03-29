package com.isp.sitesurvey.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "floors")
public class Floor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;
    
    @Column(name = "level_label", nullable = false, length = 50)
    private String levelLabel;
    
    @Column(name = "plan_file_id")
    private Long planFileId;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "floor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Space> spaces = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // --- MANUAL GETTERS & SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLevelLabel() { return levelLabel; }
    public void setLevelLabel(String levelLabel) { this.levelLabel = levelLabel; }
    public List<Space> getSpaces() { return spaces; }
    public void setSpaces(List<Space> spaces) { this.spaces = spaces; }
}