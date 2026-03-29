package com.isp.sitesurvey.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "owner_type", nullable = false, length = 50)
    private String ownerType;
    
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    
    @Column(nullable = false, length = 255)
    private String filename;
    
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;
    
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "file_data", nullable = false, columnDefinition = "MEDIUMBLOB")
    private byte[] fileData;
    
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}