package com.survey.site.service;

import com.survey.site.dto.DashboardStats;
import com.survey.site.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private FloorRepository floorRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private FloorplanRepository floorplanRepository;

    @Autowired
    private UserRepository userRepository;

    public DashboardStats getStats() {

        DashboardStats stats = new DashboardStats();

        stats.setTotalProperties(propertyRepository.count());
        stats.setTotalBuildings(buildingRepository.count());
        stats.setTotalFloors(floorRepository.count());
        stats.setTotalSpaces(spaceRepository.count());
        stats.setTotalFloorplans(floorplanRepository.count());

        stats.setTotalEngineers(
                userRepository.countByRole("ENGINEER")
        );

        return stats;
    }
}