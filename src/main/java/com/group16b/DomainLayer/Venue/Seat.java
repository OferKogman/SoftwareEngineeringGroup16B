package com.group16b.DomainLayer.Venue;

import java.util.HashMap;
import java.util.Map;

public class Seat {
	private final String seatId;
	private final int row;
	private final int number;

	private final Map<Integer, Boolean> stock;

	public Seat(int row, int number) {
		this.seatId = row + "-" + number;
		this.row = row;
		this.number = number;
		stock = new HashMap<Integer, Boolean>();
	}

	public Seat(Seat other) {
		this.seatId = other.seatId;
		this.row = other.row;
		this.number = other.number;
		this.stock = new HashMap<>(other.stock);
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

	public boolean getStock(Integer eventID) {
		return stock.get(eventID);
	}

	public boolean reserveSeat(int eventID) {
		Boolean reserved = stock.get(eventID);
		if (reserved == null) {
			throw new IllegalArgumentException("this event is not in this venue.");
		}
		if (reserved) {
			return false;
		}
		stock.put(eventID, true);
		return true;
	}

	public boolean isSeatReserved(int eventID) {
		Boolean reserved = stock.get(eventID);
		if (reserved == null) {
			throw new IllegalArgumentException("this event is not in this venue.");
		}
		return reserved;
	}

	public void returnSeat(int eventID) {
		Boolean reserved = stock.get(eventID);
		if (reserved == null) {
			throw new IllegalArgumentException("this event is not in this venue.");
		}
		if (!reserved) {
			throw new IllegalArgumentException("Seat is already free !");
		}
		stock.put(eventID, false);
	}

	public void addEvent(int eventID) {
		stock.putIfAbsent(eventID, false);
	}

	public Map<Integer, Boolean> getStock() {
		return stock;
	}
}