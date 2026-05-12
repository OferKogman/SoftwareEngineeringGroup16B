package com.group16b.ApplicationLayer.DTOs;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.group16b.DomainLayer.Venue.Seat;


public class SeatDTO {
    private String seatId;
    private int row;
    private int number;
    private final ConcurrentMap<Integer, Boolean> stock;


    public SeatDTO(Seat seat) {
        this.seatId = seat.getSeatId();
        this.row = seat.getRow();
        this.number = seat.getNumber();
        this.stock = new ConcurrentHashMap<Integer, Boolean>();

        this.stock.putAll(seat.getStock());
    }

    public ConcurrentMap<Integer, Boolean> getStock(){
        return stock;
    }
    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

}