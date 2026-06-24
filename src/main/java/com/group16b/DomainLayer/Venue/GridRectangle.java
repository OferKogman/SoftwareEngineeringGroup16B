package com.group16b.DomainLayer.Venue;

import com.group16b.ApplicationLayer.Records.GridRectangleRecord;

import jakarta.persistence.Embeddable;
@Embeddable
public class GridRectangle {
    private int startRow;
    private int startColumn;
    private int rowCount;
    private int columnCount;

    public GridRectangle(int startRow, int startColumn, int rowCount, int columnCount) {
        this.startRow = startRow;
        this.startColumn = startColumn;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
    }

    public GridRectangle(GridRectangleRecord other){
        this.startRow = other.startRow();
        this.startColumn = other.startColumn();
        this.rowCount = other.rowCount();
        this.columnCount = other.columnCount();
    }

    protected GridRectangle(){;}
    
    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public void setStartColumn(int startColumn) {
        this.startColumn = startColumn;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }
}
