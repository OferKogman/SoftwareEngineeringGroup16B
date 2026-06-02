package com.group16b.ApplicationLayer.DTOs;

public class ConfigureLayoutAndInventoryDTO {
    private int companyID, eventID;
    private String venueID;

    public ConfigureLayoutAndInventoryDTO(int companyID, int eventID, String venueID){
        this.companyID = companyID;
        this.eventID = eventID;
        this.venueID = venueID;
    }

    public int getCompanyID() {
        return companyID;
    }

    public void setCompanyID(int companyID) {
        this.companyID = companyID;
    }

    public int getEventID() {
        return eventID;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    public String getVenueID() {
        return venueID;
    }

    public void setNewVenueLayout(String venueID) {
        this.venueID = venueID;
    }
}
