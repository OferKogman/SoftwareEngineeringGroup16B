package com.group16b.DomainLayer.Venue;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.group16b.ApplicationLayer.DTOs.ChosenSeatingSegDTO;
import com.group16b.ApplicationLayer.DTOs.SeatDTO;


public class ChosenSeatingSeg extends Segment {
	protected final Map<String, Seat> seats;
	private int IDforSeat = 0;

	public ChosenSeatingSeg(String segmentID, Map<String, Seat> seats) {
		super(segmentID);
		this.seats = seats;
	}

	public ChosenSeatingSeg(ChosenSeatingSegDTO seatingSegDTO, String segmentID){
		super(segmentID);
		this.seats = new ConcurrentHashMap<>();

		for (Map.Entry<String, SeatDTO> entry : seatingSegDTO.getSeats().entrySet()) {
            seats.put(entry.getKey(), new Seat(entry.getValue(), IDforSeat+""));
			IDforSeat++;
        }
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
	public void cancelReservation(ReservationRequest request) {
		returnSeats(request.getSeatIds(), request.getEventID());
	}


	@Override
	public double getPrice(int eventID) {
		return 0.0;
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
	public void returnSeats(List<String> seatIds, int eventID) {
		for (String seatId : seatIds) {
			Seat seat = seats.get(seatId);
			if (seat != null) {
				seat.returnSeat(eventID);
			}
		}
	}

	private List<Seat> getSeatsInThisField(int eventID){
		List<Seat> list = new LinkedList<>();
		for(Seat seat : seats.values()){
			if(seat.isSeatReserved(eventID)){
				list.add(seat);
			}
		}
		return list;
	}

	@Override
	public void setStockForEvent(int eventID, int newStock){
		List<Seat> seatsInEvent = getSeatsInThisField(eventID);
		if(seatsInEvent.size() <= newStock){
			throw new IllegalArgumentException("removing too much from the segment: " + segmentID + " for event :" + eventID);
		}

		int startIndex = newStock;//we only keep the newStock amount of seats in seg

		for (int index = startIndex; index < seatsInEvent.size(); index++) {
			Seat seat = seatsInEvent.get(index);
			seat.returnSeat(eventID);
		}	
	}

	@Override
	public Map<String, Seat> getMap(){
		return seats;
	}
}
