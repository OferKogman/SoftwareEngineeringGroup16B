package com.group16b.ApplicationLayer.DTOs;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.group16b.DomainLayer.Venue.FieldSeg;

public class FieldSegDTO extends SegmentDTO {

    private final int size;
    private final GridRectangleDTO area;
    private final ConcurrentMap<Integer, Integer> stocks;

    public FieldSegDTO(FieldSeg fieldSeg) {
        super(fieldSeg);

        this.size = fieldSeg.getFieldSize();

        this.stocks = new ConcurrentHashMap<>();
        this.stocks.putAll(fieldSeg.getMap());

        this.area = new GridRectangleDTO(fieldSeg.getArea());
    }

    public GridRectangleDTO getArea() {
        return area;
    }

    public int getSize() {
        return size;
    }

    public ConcurrentMap<Integer, Integer> getStocks() {
        return stocks;
    }
}