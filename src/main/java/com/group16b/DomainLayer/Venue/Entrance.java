package com.group16b.DomainLayer.Venue;

public class Entrance {
    private final String entranceID;
    private GridRectangle area;  

    public Entrance(String entranceID, GridRectangle area){
        this.entranceID = entranceID;
        this.area = area;
    }
    public String getEntranceID() {
        return entranceID;
    }
    
    public GridRectangle getArea() {
        return area;
    }

    public void setArea(GridRectangle area) {
        this.area = area;
    }
}
