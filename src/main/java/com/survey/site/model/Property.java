

package com.survey.site.model;

import jakarta.persistence.*;

@Entity
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;

    @ManyToOne
    @JoinColumn(name = "engineer_id")
    private User engineer;

    private String status;
    private String remarks;

    @Column(length = 2000)
    private String finalReport;

    private boolean submitted = false;

    // Getters and Setters
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public User getEngineer() { return engineer; }
    public void setEngineer(User engineer) { this.engineer = engineer; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getFinalReport() { return finalReport; }
    public void setFinalReport(String finalReport) { this.finalReport = finalReport; }

    public boolean isSubmitted() { return submitted; }
    public void setSubmitted(boolean submitted) { this.submitted = submitted; }
}