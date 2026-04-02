package com.survey.site.repository;

import com.survey.site.model.SpaceCoordinate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpaceCoordinateRepository extends JpaRepository<SpaceCoordinate, Long> {

    List<SpaceCoordinate> findByFloorplanId(Long floorplanId);

    Optional<SpaceCoordinate> findBySpaceIdAndFloorplanId(Long spaceId, Long floorplanId);
}