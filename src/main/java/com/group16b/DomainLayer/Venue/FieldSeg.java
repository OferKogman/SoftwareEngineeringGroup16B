package com.group16b.DomainLayer.Venue;

import java.util.HashMap;
import java.util.Map;

public class FieldSeg extends Segment {
	private final int size;
	private Map<Integer, Integer> stock;

	public FieldSeg(String segID, int size) {
		super(segID);
		this.size = size;
		stock = new HashMap<Integer, Integer>();
	}

	public FieldSeg(FieldSeg other) {
		super(other.segmentID);
		this.size = other.size;
		this.stock = new HashMap<>(other.stock);
	}

	public int getFieldSize() {
		return size;
	}

	protected int getStock(int eventID) {
		return stock.get(eventID);
	}

	public double getPrice(int eventID) {
		return 0;
	}

	protected void addStock(int eventID, Integer quantity) {
		Integer currQty = stock.get(eventID);
		if (currQty == null) {
			throw new IllegalArgumentException("this event is not in this venue.");
		}
		if (currQty + quantity > size) {
			throw new IllegalArgumentException("Field is not big enough !");
		}
		stock.put(eventID, currQty + quantity);
	}

	@Override
	public void setStockForEvent(int eventID, int newStock) {
		if (!this.stock.containsKey(eventID)) {
			throw new IllegalArgumentException("cannot modify segments unrelated to event");
		}
		stock.put(eventID, newStock);// maybe add a maximum number?
	}

	protected void removeStock(int eventID, Integer quantity) {
		Integer currQty = stock.get(eventID);
		if (currQty == null) {
			throw new IllegalArgumentException("this event is not in this venue.");
		}
		if (currQty - quantity < 0) {
			throw new IllegalArgumentException("Not enough tickets left in stock !");
		}
		stock.put(eventID, currQty - quantity);
	}

	@Override
	public void reserve(ReservationRequest request) {
		removeStock(request.getEventID(), request.getQuantity());
	}

	@Override
	public void cancelReservation(ReservationRequest request) {
		addStock(request.getEventID(), request.getQuantity());
	}

	protected void addEvent(int eventID) {
		stock.putIfAbsent(eventID, size);
	}

	@Override
	public String getSegmentType() {
		return "F";
	}

	@Override
	public Map<Integer, Integer> getMap() {
		return stock;
	}
}
