
package com.survey.site.dto;

import java.util.List;

public class FloorRequestDTO {

    private String name;
    private Long buildingId;

    private List<FloorplanDTO> floorplans;

    public static class FloorplanDTO {
        private String name;
        private List<SpaceDTO> spaces;

        public static class SpaceDTO {
            private String name;
            private String type;
            private Double area;
            private Double height;
            private String geometry;

            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            public String getType() { return type; }
            public void setType(String type) { this.type = type; }
            public Double getArea() { return area; }
            public void setArea(Double area) { this.area = area; }
            public Double getHeight() { return height; }
            public void setHeight(Double height) { this.height = height; }
            public String getGeometry() { return geometry; }
            public void setGeometry(String geometry) { this.geometry = geometry; }
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<SpaceDTO> getSpaces() { return spaces; }
        public void setSpaces(List<SpaceDTO> spaces) { this.spaces = spaces; }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getBuildingId() { return buildingId; }
    public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }
    public List<FloorplanDTO> getFloorplans() { return floorplans; }
    public void setFloorplans(List<FloorplanDTO> floorplans) { this.floorplans = floorplans; }
}