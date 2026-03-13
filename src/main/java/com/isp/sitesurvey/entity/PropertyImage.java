package com.isp.sitesurvey.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "property_images")
@Data
public class PropertyImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "property_id")
    private Property property;

    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] imageData;

    private String contentType;
}