
package com.survey.site.repository;

import com.survey.site.model.Space;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpaceRepository extends JpaRepository<Space, Long> {

    List<Space> findByFloorId(Long floorId);


    Optional<Space> findByNameAndFloorId(String name, Long floorId);

    List<Space> findByFloorBuildingPropertyId( Long floorId);
}