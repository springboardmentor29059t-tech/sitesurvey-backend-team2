

package com.survey.site.model;

import jakarta.persistence.*;

@Entity
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String checklist;
    private String remarks;

    private String networkType;
    private String signalValue;

    private String status;

    @ManyToOne
    @JoinColumn(name = "floor_id")
    private Floor floor;

    @ManyToOne
    @JoinColumn(name = "space_id")
    private Space space;

    @ManyToOne
    @JoinColumn(name = "engineer_id")
    private User engineer;

    public Survey() {}

    public Long getId() {
        return id;
    }

    public String getChecklist() {
        return checklist;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getNetworkType() {
        return networkType;
    }

    public String getSignalValue() {
        return signalValue;
    }

    public String getStatus() {
        return status;
    }

    public Space getSpace() {
        return space;
    }

    public User getEngineer() {
        return engineer;
    }

    public void setChecklist(String checklist) {
        this.checklist = checklist;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public void setSignalValue(String signalValue) {
        this.signalValue = signalValue;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSpace(Space space) {
        this.space = space;
    }

    public void setEngineer(User engineer) {
        this.engineer = engineer;
    }
}