package com.group16b.DomainLayer.Venue;

public class Stage {
    private final String stageID;
    private GridRectangle area;

    public Stage(String stageID, GridRectangle area) {
        this.stageID = stageID;
        this.area = area;
    }

    public String getStageID() {
        return stageID;
    }

    public GridRectangle getArea() {
        return area;
    }
    public void setArea(GridRectangle area) {
        this.area = area;
    }    
}
