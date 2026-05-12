package com.group16b.ApplicationLayer.DTOs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.group16b.DomainLayer.Venue.ChosenSeatingSeg;
import com.group16b.DomainLayer.Venue.Seat;

public class ChosenSeatingSegDTO extends SegmentDTO{
    protected final Map<String, SeatDTO> seats;

    public ChosenSeatingSegDTO(ChosenSeatingSeg seatingSeg){
        super(seatingSeg);
        seats = new ConcurrentHashMap<>();

        for (Map.Entry<String, Seat> entry : seatingSeg.getMap().entrySet()) {
            seats.put(entry.getKey(), new SeatDTO(entry.getValue()));
        }

    }
    
    @Override
    public String getSegmentType(){
        return "S";
    }

    public Map<String, SeatDTO> getSeats(){
        return seats;
    }
}
