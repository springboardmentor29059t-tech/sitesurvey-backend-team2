package com.survey.site.model;

import jakarta.persistence.*;

@Entity
@Table(name = "engineers")
public class Engineer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String phone;

    // OrgAdmin who created this engineer
    @Column(name = "org_admin_id")
    private Long orgAdminId;

    public Engineer() {}

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Long getOrgAdminId() {
        return orgAdminId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setOrgAdminId(Long orgAdminId) {
        this.orgAdminId = orgAdminId;
    }



}
