package com.isp.sitesurvey.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "property_images")
@Data
@ToString(exclude = "property")
public class PropertyImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore // CRITICAL: Stops loop: Property -> Image -> Property
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property;

    @JsonIgnore // Prevents huge JSON payloads
    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] imageData;

    private String contentType;
}