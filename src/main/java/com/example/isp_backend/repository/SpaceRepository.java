package com.example.isp_backend.repository;

import com.example.isp_backend.entity.Space;
import com.example.isp_backend.entity.Floor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpaceRepository extends JpaRepository<Space, Long> {

    List<Space> findByFloor(Floor floor);
}
