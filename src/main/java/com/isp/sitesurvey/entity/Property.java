package com.isp.sitesurvey.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    private String city;
    private String state;
    private String country;

    @Column(name = "postal_code")
    private String postalCode;

    @Lob
    @JsonIgnore 
    @Column(name = "image_data", columnDefinition = "MEDIUMBLOB")
    private byte[] imageData;

    @Column(name = "image_content_type")
    private String imageContentType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // ✅ FIX 1: The missing field that caused "cannot be resolved"
    @JsonIgnore
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PropertyImage> extraImages = new ArrayList<>();

    // ✅ FIX 2: Added Equipment relationship with Cascade to fix your Delete error
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Equipment> equipments = new ArrayList<>();

    public Property() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // =======================
    // GETTERS AND SETTERS
    // =======================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public byte[] getImageData() { return imageData; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }

    public String getImageContentType() { return imageContentType; }
    public void setImageContentType(String imageContentType) { this.imageContentType = imageContentType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<PropertyImage> getExtraImages() { return extraImages; }
    public void setExtraImages(List<PropertyImage> extraImages) { this.extraImages = extraImages; }

    public List<Equipment> getEquipments() { return equipments; }
    public void setEquipments(List<Equipment> equipments) { this.equipments = equipments; }
}