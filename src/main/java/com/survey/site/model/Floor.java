

package com.survey.site.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
public class Floor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer floorNumber;

    @ManyToOne(fetch = FetchType.EAGER) // Load building info automatically
    @JoinColumn(name = "building_id", nullable = false)
    @JsonIgnoreProperties({"property", "floors"})
    private Building building;

    // Constructors
    public Floor() {}

    public Floor(String name, Integer floorNumber, Building building) {
        this.name = name;
        this.floorNumber = floorNumber;
        this.building = building;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }

    public Building getBuilding() { return building; }
    public void setBuilding(Building building) { this.building = building; }
}