package com.group16b.ApplicationLayer.DTOs;

import com.group16b.ApplicationLayer.Records.VenueRecord;

public class ConfigureNewLayoutAndInventoryDTO {
    private int companyID;
    private VenueRecord newVenueLayout;

    public ConfigureNewLayoutAndInventoryDTO() {
    }

    public ConfigureNewLayoutAndInventoryDTO(int companyID, VenueRecord newVenueLayout) {
        this.companyID = companyID;
        this.newVenueLayout = newVenueLayout;
    }

    public int getCompanyID() {
        return companyID;
    }

    public void setCompanyID(int companyID) {
        this.companyID = companyID;
    }

    public VenueRecord getNewVenueLayout() {
        return newVenueLayout;
    }

    public void setNewVenueLayout(VenueRecord newVenueLayout) {
        this.newVenueLayout = newVenueLayout;
    }
}
