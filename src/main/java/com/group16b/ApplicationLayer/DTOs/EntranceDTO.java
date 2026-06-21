package com.group16b.ApplicationLayer.DTOs;

import com.group16b.DomainLayer.Venue.Entrance;

public class EntranceDTO {
    private String entranceID;
    private GridRectangleDTO area;

    public EntranceDTO(String entranceID, GridRectangleDTO area) {
        this.entranceID = entranceID;
        this.area = area;
    }

    public EntranceDTO(Entrance entrance) {
        this.entranceID = entrance.getEntranceID();
        this.area = new GridRectangleDTO(entrance.getArea());
    }

    public String getEntranceID() {
        return entranceID;
    }

    public GridRectangleDTO getArea() {
        return area;
    }
}
