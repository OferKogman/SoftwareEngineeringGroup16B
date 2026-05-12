package com.group16b.ApplicationLayer.DTOs;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.group16b.DomainLayer.Venue.FieldSeg;

public class FieldSegDTO extends SegmentDTO{

    private final int size;

	private ConcurrentMap<Integer, Integer> stocks;

    public FieldSegDTO(FieldSeg fieldSeg){
        super(fieldSeg);
        this.size = fieldSeg.getFieldSize();

        stocks = new ConcurrentHashMap<>();

        this.stocks.putAll(fieldSeg.getMap());
    } 
    
    @Override
    public String getSegmentType(){
        return "F";
    }

    public int getSize(){
        return size;
    }

    public ConcurrentMap<Integer, Integer> getStocks(){
        return stocks;
    }
}
