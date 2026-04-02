
package com.survey.site.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.List;

@Entity
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "property_id")
    @JsonIgnoreProperties({"engineer"})
    private Property property;

    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Floor> floors;

    // Constructors
    public Building() {}

    public Building(String name, Property property) {
        this.name = name;
        this.property = property;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }

    public List<Floor> getFloors() { return floors; }
    public void setFloors(List<Floor> floors) { this.floors = floors; }
}