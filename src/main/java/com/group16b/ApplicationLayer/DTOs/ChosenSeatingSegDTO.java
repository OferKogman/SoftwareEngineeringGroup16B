package com.group16b.ApplicationLayer.DTOs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.group16b.DomainLayer.Venue.ChosenSeatingSeg;
import com.group16b.DomainLayer.Venue.Seat;

public class ChosenSeatingSegDTO extends SegmentDTO {

    private final ConcurrentMap<String, SeatDTO> seats;
    private final GridRectangleDTO area;

    public ChosenSeatingSegDTO(ChosenSeatingSeg chosenSeatingSeg) {
        super(chosenSeatingSeg);

        this.seats = new ConcurrentHashMap<>();

        for (Map.Entry<String, Seat> entry : chosenSeatingSeg.getMap().entrySet()) {
            this.seats.put(entry.getKey(), new SeatDTO(entry.getValue()));
        }

        this.area = new GridRectangleDTO(chosenSeatingSeg.getArea());
    }

    public ConcurrentMap<String, SeatDTO> getSeats() {
        return seats;
    }

    public GridRectangleDTO getArea() {
        return area;
    }
}