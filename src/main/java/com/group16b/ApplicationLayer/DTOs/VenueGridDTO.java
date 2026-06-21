package com.group16b.ApplicationLayer.DTOs;

import com.group16b.DomainLayer.Venue.VenueGrid;

public class VenueGridDTO {
    private int rows;
    private int columns;

    public VenueGridDTO(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
    }

    public VenueGridDTO(VenueGrid venueGrid) {
        this.rows = venueGrid.getRows();
        this.columns = venueGrid.getColumns();
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }
}
