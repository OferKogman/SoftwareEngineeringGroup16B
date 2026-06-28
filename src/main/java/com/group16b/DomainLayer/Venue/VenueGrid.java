package com.group16b.DomainLayer.Venue;

import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable
public class VenueGrid {
    private int rows;
    private int columns;

    public VenueGrid(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
    }

    protected VenueGrid(){};

    public int getRows() { return rows; }
    public int getColumns() { return columns; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VenueGrid)) {
            return false;
        }
        VenueGrid other = (VenueGrid) o;
        return rows == other.rows &&
            columns == other.columns;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rows, columns);
    }
}