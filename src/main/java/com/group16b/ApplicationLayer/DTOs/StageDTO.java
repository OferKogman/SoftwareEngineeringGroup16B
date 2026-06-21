package com.group16b.ApplicationLayer.DTOs;

import com.group16b.DomainLayer.Venue.Stage;

public class StageDTO {
    private String stageID;
    private GridRectangleDTO area;

    public StageDTO(String stageID, GridRectangleDTO area) {
        this.stageID = stageID;
        this.area = area;
    }

    public StageDTO(Stage stage) {
        this.stageID = stage.getStageID();
        this.area = new GridRectangleDTO(stage.getArea());
    }

    public String getStageID() {
        return stageID;
    }

    public GridRectangleDTO getArea() {
        return area;
    }
}
