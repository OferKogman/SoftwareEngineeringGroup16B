package com.group16b.DomainLayer.Event;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class Seat {
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

	protected String getSeatId() {
		return seatId;
	}

	protected int getRow() {
		return row;
	}

	protected int getNumber() {
		return number;
	}

	protected boolean getStock(int eventID) {
		return stock.get(eventID);
	}

	protected void reserveSeat(int eventID) {
		while (true) {
			Boolean reserved = stock.get(eventID);
			if (reserved == null) {
				throw new IllegalArgumentException("this event is not in this venue.");
			}
			if (reserved) {
				throw new IllegalArgumentException("Seat is already reserved !");
			}
			if (stock.replace(eventID, reserved, true)) {
				return;
			}
		}
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
}