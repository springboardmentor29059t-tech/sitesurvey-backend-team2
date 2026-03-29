package com.isp.sitesurvey.repository;

import com.isp.sitesurvey.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    // Custom query to fetch all equipment for a specific map
    List<Equipment> findByPropertyId(Long propertyId);
}