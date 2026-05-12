package com.group16b.DomainLayer.Venue;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.group16b.ApplicationLayer.DTOs.SeatDTO;

public class Seat {
	private final String seatId;
	private final int row;
	private final int number;

	private final ConcurrentMap<Integer, Boolean> stock;

	protected Seat(int row, int number) {
		this.seatId = row + "-" + number;
		this.row = row;
		this.number = number;
		stock = new ConcurrentHashMap<Integer, Boolean>();
	}

	public Seat(SeatDTO seatDTO, String seatID){
		this.seatId = seatID;
		this.row = seatDTO.getRow();
		this.number = seatDTO.getNumber();
		
		this.stock = new ConcurrentHashMap<>();
		stock.putAll(seatDTO.getStock());
	}

	public String getSeatId() {
		return seatId;
	}

	public int getRow() {
		return row;
	}

	public int getNumber() {
		return number;
	}

	protected boolean getStock(Integer eventID) {
		return stock.get(eventID);
	}

	protected boolean reserveSeat(int eventID) {
		while (true) {
			Boolean reserved = stock.get(eventID);
			if (reserved == null) {
				throw new IllegalArgumentException("this event is not in this venue.");
			}
			if (reserved) {
				return false;
			}
			if (stock.replace(eventID, reserved, true)) {
				return true;
			}
		}
	}
	
	protected boolean isSeatReserved(int eventID) {
		Boolean reserved = stock.get(eventID);
		if (reserved == null) {
			throw new IllegalArgumentException("this event is not in this venue.");
		}
		return reserved;
	}

	protected void returnSeat(int eventID) {
		while (true) {
			Boolean reserved = stock.get(eventID);
			if (reserved == null) {
				throw new IllegalArgumentException("this event is not in this venue.");
			}
			if (!reserved) {
				throw new IllegalArgumentException("Seat is already free !");
			}
			if (stock.replace(eventID, reserved, false)) {
				return;
			}
		}
	}

	protected void addEvent(int eventID) {
		stock.putIfAbsent(eventID, false);
	}

	public ConcurrentMap<Integer, Boolean> getStock(){
		return stock;
	}
}