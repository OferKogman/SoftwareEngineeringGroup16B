package com.group16b.DomainLayer.Venue;

import jakarta.persistence.Embeddable;

@Embeddable
public class VenueGrid {
    private int rows;
    private int columns;

    public VenueGrid(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
    }

    public int getRows() { return rows; }
    public int getColumns() { return columns; }
}