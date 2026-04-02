package com.survey.site.model;

import jakarta.persistence.*;

@Entity
@Table(name="engineer_assignments")
public class EngineerAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long engineerId;

    private Long buildingId;

    private Long floorId;

    private String status; // Pending / Completed

    public EngineerAssignment(){}

    public Long getId(){ return id; }
    public void setId(Long id){ this.id=id; }

    public Long getEngineerId(){ return engineerId; }
    public void setEngineerId(Long engineerId){ this.engineerId=engineerId; }

    public Long getBuildingId(){ return buildingId; }
    public void setBuildingId(Long buildingId){ this.buildingId=buildingId; }

    public Long getFloorId(){ return floorId; }
    public void setFloorId(Long floorId){ this.floorId=floorId; }

    public String getStatus(){ return status; }
    public void setStatus(String status){ this.status=status; }
}