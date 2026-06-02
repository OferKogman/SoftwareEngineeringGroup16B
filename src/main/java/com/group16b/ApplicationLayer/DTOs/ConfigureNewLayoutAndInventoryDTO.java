package com.group16b.ApplicationLayer.DTOs;

import com.group16b.ApplicationLayer.Records.VenueRecord;

public class ConfigureNewLayoutAndInventoryDTO {
    private int companyID, eventID;
    private VenueRecord newVenueLayout;

    public ConfigureNewLayoutAndInventoryDTO(int companyID, int eventID, VenueRecord newVenueLayout){
        this.companyID = companyID;
        this.eventID = eventID;
        this.newVenueLayout = newVenueLayout;
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

    public VenueRecord getNewVenueLayout() {
        return newVenueLayout;
    }

    public void setNewVenueLayout(VenueRecord newVenueLayout) {
        this.newVenueLayout = newVenueLayout;
    }
}
