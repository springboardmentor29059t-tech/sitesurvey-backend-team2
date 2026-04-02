//
//
//package com.survey.site.model;
//
//import jakarta.persistence.*;
//
//@Entity
//public class Space {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String name;
//
//    private Double area;
//    private Integer rfStrength; // e.g., -65
//    private String rfStatus;    // GOOD / MEDIUM / POOR
//
//    @ManyToOne
//    @JoinColumn(name = "engineer_id")
//    private Engineer engineer;
//    @ManyToOne
//    @JoinColumn(name="floor_id")
//    private Floor floor;
//
//    public Space(){}
//
//    public Long getId() {
//        return id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public Double getArea() {
//        return area;
//    }
//
//    public Floor getFloor() {
//        return floor;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public void setArea(Double area) {
//        this.area = area;
//    }
//
//    public void setFloor(Floor floor) {
//        this.floor = floor;
//    }
//    public Engineer getEngineer() {
//        return engineer;
//    }
//
//    public void setEngineer(Engineer engineer) {
//        this.engineer = engineer;
//    }
//
//    public Integer getRfStrength() {
//        return rfStrength;
//    }
//
//    public void setRfStrength(Integer rfStrength) {
//        this.rfStrength = rfStrength;
//    }
//
//    public String getRfStatus() {
//        return rfStatus;
//    }
//
//    public void setRfStatus(String rfStatus) {
//        this.rfStatus = rfStatus;
//    }
//}


package com.survey.site.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double area;

    private Integer rfStrength;
    private String rfStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "engineer_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Engineer engineer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "floor_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "building"})
    private Floor floor;

    public Space() {}

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getArea() {
        return area;
    }

    public Integer getRfStrength() {
        return rfStrength;
    }

    public String getRfStatus() {
        return rfStatus;
    }

    public Engineer getEngineer() {
        return engineer;
    }

    public Floor getFloor() {
        return floor;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setArea(Double area) {
        this.area = area;
    }

    public void setRfStrength(Integer rfStrength) {
        this.rfStrength = rfStrength;
    }

    public void setRfStatus(String rfStatus) {
        this.rfStatus = rfStatus;
    }

    public void setEngineer(Engineer engineer) {
        this.engineer = engineer;
    }

    public void setFloor(Floor floor) {
        this.floor = floor;
    }
}