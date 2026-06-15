package com.group16b.ApplicationLayer.DTOs;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.group16b.ApplicationLayer.Records.GridRectangleRecord;
import com.group16b.DomainLayer.Venue.FieldSeg;
import com.group16b.DomainLayer.Venue.GridRectangle;

public class FieldSegDTO extends SegmentDTO {

    private final int size;
    private final GridRectangleRecord area;
    private final ConcurrentMap<Integer, Integer> stocks;

    public FieldSegDTO(FieldSeg fieldSeg) {
        super(fieldSeg);

        this.size = fieldSeg.getFieldSize();

        this.stocks = new ConcurrentHashMap<>();
        this.stocks.putAll(fieldSeg.getMap());

        GridRectangle currArea = fieldSeg.getArea();
        this.area = new GridRectangleRecord(
                currArea.getStartRow(),
                currArea.getStartColumn(),
                currArea.getRowCount(),
                currArea.getColumnCount()
        );
    }

    @Override
    public String getSegmentType() {
        return "F";
    }

    public GridRectangleRecord getArea() {
        return area;
    }

    public int getSize() {
        return size;
    }

    public ConcurrentMap<Integer, Integer> getStocks() {
        return stocks;
    }
}