package com.isp.sitesurvey.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "equipment")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // e.g., "router", "switch"

    @Column(name = "geometry_wkt", nullable = false)
    private String geometryWkt; // Holds "POINT(lng lat)"

    @Column(name = "power_watts")
    private Integer powerWatts;

    /**
     * RSSI in dBm as recorded from the RF scan tool.
     * Null if the device was placed manually on the canvas.
     */
    @Column(name = "rssi")
    private Integer rssi;

    // Link this equipment to a specific property
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    @JsonIgnore
    private Property property;

    // Default constructor
    public Equipment() {}

    // =========================
    // Getters and Setters
    // =========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGeometryWkt() {
        return geometryWkt;
    }

    public void setGeometryWkt(String geometryWkt) {
        this.geometryWkt = geometryWkt;
    }

    public Integer getPowerWatts() {
        return powerWatts;
    }

    public void setPowerWatts(Integer powerWatts) {
        this.powerWatts = powerWatts;
    }

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }
}