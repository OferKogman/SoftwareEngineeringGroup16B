package com.group16b.DomainLayer.Venue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.group16b.ApplicationLayer.Records.SeatRecord;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "chosen_seating_segments")
public class ChosenSeatingSeg extends Segment {
    private int IDforSeat = 0;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "segment_db_id") // Connects to Segment's db_id
    @MapKey(name = "seatId") 
    protected Map<String, Seat> seats = new HashMap<>();

    public ChosenSeatingSeg(String segmentID, Map<String, Seat> seats, GridRectangle area) {
        super(segmentID, area);
        this.seats = seats;
    }

    public ChosenSeatingSeg() { }

    public ChosenSeatingSeg(String segmentID, List<SeatRecord> seats, GridRectangle area) {
        super(segmentID, area);
        this.seats = new ConcurrentHashMap<>();
        for (SeatRecord sr : seats) {
            String seatId = sr.row() + "-" + sr.number();
            this.seats.put(seatId, new Seat(sr.row(), sr.number()));
        }
    }

    public ChosenSeatingSeg(ChosenSeatingSeg other) {
        super(other);
        this.IDforSeat = other.IDforSeat;
        this.seats = new ConcurrentHashMap<>();
        for (Map.Entry<String, Seat> entry : other.seats.entrySet()) {
            this.seats.put(entry.getKey(), new Seat(entry.getValue()));
        }
    }

    @Override
    public String getSegmentType() { return "S"; }

    @Override
    public void reserve(ReservationRequest request) {
        reserveSeats(request.getSeatIds(), request.getEventID());
    }

    @Override
    public void cancelReservation(ReservationRequest request) {
        returnSeats(request.getSeatIds(), request.getEventID());
    }

    void reserveSeats(List<String> seatIds, int eventID) {
        List<Seat> reservedSeats = new java.util.ArrayList<>();
        for (String seatId : seatIds) {
            Seat seat = seats.get(seatId);
            if (seat == null) {
                for (Seat _seat : reservedSeats) _seat.returnSeat(eventID);
                throw new IllegalArgumentException("Seat with ID " + seatId + " not found");
            }
            if (!seat.reserveSeat(eventID)) {
                for (Seat _seat : reservedSeats) _seat.returnSeat(eventID);
                throw new IllegalArgumentException("Failed to reserve seat with ID " + seatId);
            }
            reservedSeats.add(seat);
        }
    }

    public void returnSeats(List<String> seatIds, int eventID) {
        for (String seatId : seatIds) {
            Seat seat = seats.get(seatId);
            if (seat != null) seat.returnSeat(eventID);
        }
    }

    private List<Seat> getSeatsInThisField(int eventID) {
        List<Seat> list = new LinkedList<>();
        for (Seat seat : seats.values()) {
            if (seat.isSeatReserved(eventID)) {
                list.add(seat);
            }
        }
        return list;
    }

    public void setNewStock(List<String> newSeatsIDs, List<Integer> eventIDsToInitialize) {
        List<String> refundSeatsIDs = new LinkedList<>();
        for (String oldSeatID : this.seats.keySet()) {
            if (!newSeatsIDs.contains(oldSeatID)) refundSeatsIDs.add(oldSeatID);
        }

        List<String> addSeatsIDs = new LinkedList<>();
        for (String newSeatID : newSeatsIDs) {
            if (!this.seats.keySet().contains(newSeatID)) addSeatsIDs.add(newSeatID);
        }
        
        for (String addSeatID : addSeatsIDs) validateSeatIDFormat(addSeatID);
        
        for (String addSeatID : addSeatsIDs) {
            String[] parts = addSeatID.split("-");
            int row = Integer.parseInt(parts[0]);
            int column = Integer.parseInt(parts[1]);
            Seat newSeat = new Seat(row, column);
            for (Integer eventID : eventIDsToInitialize) {
                newSeat.addEvent(eventID);
            }
            seats.put(addSeatID, newSeat);
        }

        for (String refundSeatID : refundSeatsIDs) seats.remove(refundSeatID);
    }

    public void setNewStock(List<String> newSeatsIDs) {
        List<String> refundSeatsIDs = new LinkedList<>();
        for (String oldSeatID : this.seats.keySet()) {
            if (!newSeatsIDs.contains(oldSeatID)) refundSeatsIDs.add(oldSeatID);
        }
        
        List<String> addSeatsIDs = new LinkedList<>();
        for (String newSeatID : newSeatsIDs) {
            if (!this.seats.keySet().contains(newSeatID)) addSeatsIDs.add(newSeatID);
        }
        
        for (String addSeatID : addSeatsIDs) validateSeatIDFormat(addSeatID);

        for (String addSeatID : addSeatsIDs) {
            String[] parts = addSeatID.split("-");
            int row = Integer.parseInt(parts[0]);
            int column = Integer.parseInt(parts[1]);
            seats.put(addSeatID, new Seat(row, column));
        }

        for (String refundSeatID : refundSeatsIDs) seats.remove(refundSeatID);
    }

    public List<String> getStockRefundForEvent(int eventID, List<String> newSeatsIDs) {
        List<Seat> seatsInEvent = getSeatsInThisField(eventID);
        List<String> oldSeatsIDs = new LinkedList<>();
        for (Seat seat : seatsInEvent) oldSeatsIDs.add(seat.getSeatId());

        List<String> refundSeatsIDs = new LinkedList<>();
        for (String oldSeatID : oldSeatsIDs) {
            if (!newSeatsIDs.contains(oldSeatID)) refundSeatsIDs.add(oldSeatID);
        }

        List<String> refundSeats = new LinkedList<>();
        for (String refundSeatID : refundSeatsIDs) {
            Seat seat = seats.get(refundSeatID);
            if (seat == null) throw new IllegalArgumentException("Seat with ID " + refundSeatID + " not found for refund");
            if (seat.isSeatReserved(eventID)) refundSeats.add(refundSeatID);
        }
        return refundSeats;
    }

    private void validateSeatIDFormat(String seatID) {
        String[] parts = seatID.split("-");
        if (parts.length != 2) throw new IllegalArgumentException("Invalid seat ID format: " + seatID);
        try {
            Integer.parseInt(parts[0]);
            Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid seat ID format: " + seatID);
        }
    }

    @Override
    public Map<String, Seat> getMap() { return seats; }

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChosenSeatingSeg other)) return false;
        return segmentID==other.segmentID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(segmentID);
    }
}