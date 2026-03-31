package com.isp.sitesurvey.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "spaces")
public class Space {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id", nullable = false)
    private Floor floor;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 50)
    private String type;
    
    @Column(name = "area_sqm", precision = 10, scale = 2)
    private BigDecimal areaSqm;
    
    @Column(columnDefinition = "TEXT")
    private String notes;

    // ✅ ADDED NEW FIELDS HERE
    @Column(name = "elevation_m", precision = 10, scale = 2)
    private BigDecimal elevationM;

    @Column(name = "geometry_wkt", columnDefinition = "TEXT")
    private String geometryWkt;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // --- MANUAL GETTERS & SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Floor getFloor() { return floor; }
    public void setFloor(Floor floor) { this.floor = floor; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getAreaSqm() { return areaSqm; }
    public void setAreaSqm(BigDecimal areaSqm) { this.areaSqm = areaSqm; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // ✅ ADDED NEW GETTERS & SETTERS HERE
    public BigDecimal getElevationM() { return elevationM; }
    public void setElevationM(BigDecimal elevationM) { this.elevationM = elevationM; }

    public String getGeometryWkt() { return geometryWkt; }
    public void setGeometryWkt(String geometryWkt) { this.geometryWkt = geometryWkt; }
}