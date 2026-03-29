package com.isp.sitesurvey.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "property_extra_images")
public class PropertyExtraImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] imageData;

    private String imageContentType;

    @ManyToOne
    @JoinColumn(name = "property_id")
    private Property property;

    public PropertyExtraImage() {}

    public Long getId() {
        return id;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public String getImageContentType() {
        return imageContentType;
    }

    public void setImageContentType(String imageContentType) {
        this.imageContentType = imageContentType;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }
}