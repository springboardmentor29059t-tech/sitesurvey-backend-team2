package com.isp.sitesurvey.repository;

import com.isp.sitesurvey.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    List<FileEntity> findByOwnerTypeAndOwnerId(String ownerType, Long ownerId);
    
    @Query("SELECT f FROM FileEntity f WHERE f.ownerType = ?1 AND f.ownerId = ?2")
    List<FileEntity> findFilesByOwner(String ownerType, Long ownerId);
}