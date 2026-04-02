package com.survey.site.repository;

import com.survey.site.model.Building;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BuildingRepository extends JpaRepository<Building,Long> {
    List<Building> findByPropertyId(Long propertyId);

}