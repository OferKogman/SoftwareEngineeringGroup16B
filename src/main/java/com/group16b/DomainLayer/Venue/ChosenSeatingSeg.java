package com.group16b.DomainLayer.Venue;

import java.util.List;
import java.util.Map;

public class ChosenSeatingSeg extends Segment {
	protected final Map<String, Seat> seats;

	ChosenSeatingSeg(String segmentID, Map<String, Seat> seats) {
		super(segmentID);
		this.seats = seats;
	}

	@Override
	public String getSegmentType() {
		return "S";
	}

	@Override
	public void reserve(ReservationRequest request) {
		reserveSeats(request.getSeatIds(), request.getEventID());
	}
	@Override
	public double getPrice(int eventID) {
		throw new UnsupportedOperationException("Price calculation is not implemented yet.");
	}

	void reserveSeats(List<String> seatIds, int eventID) {
		List<Seat> reservedSeats = new java.util.ArrayList<>();
		for (String seatId : seatIds) {
			Seat seat = seats.get(seatId);
			if (seat == null) {
				for (Seat _seat : reservedSeats) {
					_seat.returnSeat(eventID);
				}
				throw new IllegalArgumentException("Seat with ID " + seatId + " not found");
			}
			if (!seat.reserveSeat(eventID)){
				for (Seat _seat : reservedSeats) {
					_seat.returnSeat(eventID);
				}
				throw new IllegalArgumentException("Failed to reserve seat with ID " + seatId);
			}
			reservedSeats.add(seat);
		}
	}
}
