package com.isp.sitesurvey.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "checklist_templates")
public class ChecklistTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "target_role")
    private String targetRole;

    // ✅ FIX: Added FetchType.EAGER to solve the "no Session" error
    // ✅ REMOVED: @JsonManagedReference (not needed because of @JsonIgnore in the Item class)
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ChecklistItem> items = new ArrayList<>();

    public ChecklistTemplate() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }
    public List<ChecklistItem> getItems() { return items; }
    public void setItems(List<ChecklistItem> items) { this.items = items; }
}