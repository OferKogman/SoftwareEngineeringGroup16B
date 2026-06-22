package com.group16b.ApplicationLayer.DTOs;

import com.group16b.DomainLayer.Venue.GridRectangle;

public class GridRectangleDTO {
    private int startRow;
    private int startColumn;
    private int rowCount;
    private int columnCount;

    public GridRectangleDTO(int startRow, int startColumn, int rowCount, int columnCount) {
        this.startRow = startRow;
        this.startColumn = startColumn;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
    }

    public GridRectangleDTO(GridRectangle grid) {
        this.startRow = grid.getStartRow();
        this.startColumn = grid.getStartColumn();
        this.rowCount = grid.getRowCount();
        this.columnCount = grid.getColumnCount();
    }

    public int getStartRow() {
        return startRow;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }
}