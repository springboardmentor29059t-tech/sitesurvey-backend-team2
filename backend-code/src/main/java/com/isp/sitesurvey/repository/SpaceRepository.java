package com.isp.sitesurvey.repository;

import com.isp.sitesurvey.entity.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SpaceRepository extends JpaRepository<Space, Long> {
    List<Space> findByFloorId(Long floorId);
    long countByFloorId(Long floorId);
}