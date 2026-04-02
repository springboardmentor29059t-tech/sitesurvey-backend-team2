package com.survey.site.dto;

public class DashboardStats {

    private long totalProperties;
    private long totalBuildings;
    private long totalFloors;
    private long totalSpaces;
    private long totalFloorplans;
    private long totalEngineers;

    public DashboardStats() {}

    public long getTotalProperties() {
        return totalProperties;
    }

    public void setTotalProperties(long totalProperties) {
        this.totalProperties = totalProperties;
    }

    public long getTotalBuildings() {
        return totalBuildings;
    }

    public void setTotalBuildings(long totalBuildings) {
        this.totalBuildings = totalBuildings;
    }

    public long getTotalFloors() {
        return totalFloors;
    }

    public void setTotalFloors(long totalFloors) {
        this.totalFloors = totalFloors;
    }

    public long getTotalSpaces() {
        return totalSpaces;
    }

    public void setTotalSpaces(long totalSpaces) {
        this.totalSpaces = totalSpaces;
    }

    public long getTotalFloorplans() {
        return totalFloorplans;
    }

    public void setTotalFloorplans(long totalFloorplans) {
        this.totalFloorplans = totalFloorplans;
    }

    public long getTotalEngineers() {
        return totalEngineers;
    }

    public void setTotalEngineers(long totalEngineers) {
        this.totalEngineers = totalEngineers;
    }
}