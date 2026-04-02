
package com.survey.site.repository;

import com.survey.site.model.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FloorRepository extends JpaRepository<Floor, Long> {
    List<Floor> findByBuildingPropertyId(Long propertyId);
    List<Floor> findByBuilding_Id(Long buildingId);
    List<Floor> findByBuildingId(Long buildingId);
}