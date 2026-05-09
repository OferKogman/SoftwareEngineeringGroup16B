package com.group16b.DomainLayer.Venue;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FieldSeg extends Segment {
	private final int size;

	private ConcurrentMap<Integer, Integer> stock;

	public FieldSeg(String segID, int size) {
		super(segID);
		this.size = size;
		stock = new ConcurrentHashMap<Integer, Integer>();
	}

	protected int getFieldSize() {
		return size;
	}

	protected int getStock(int eventID) {
		return stock.get(eventID);
	}

	protected void addStock(int eventID, Integer quantity) {
		while (true) {
			Integer currQty = stock.get(eventID);
			if (currQty == null) {
				throw new IllegalArgumentException("this event is not in this venue.");
			} else {
				if (currQty + quantity > size) {
					throw new IllegalArgumentException("Field is not big enough !");
				}
				if (stock.replace(eventID, currQty, currQty + quantity)) {
					return;
				}
			}
		}
	}

	protected void removeStock(int eventID, Integer quantity) {
		while (true) {
			Integer currQty = stock.get(eventID);
			if (currQty == null) {
				throw new IllegalArgumentException("this event is not in this venue.");
			}
			if (currQty - quantity < 0) {
				throw new IllegalArgumentException("Not enough tickets left in stock !");
			}
			if (stock.replace(eventID, currQty, currQty - quantity)) {
				return;
			}
		}
	}
	@Override
	protected void reserve(ReservationRequest request) {
		reserveInField(request.getEventID(), request.getQuantity());
	}

	private void reserveInField(int eventID, Integer quantity){
		while (true) {
			Integer currQty = stock.get(eventID);
			if (currQty == null) {
				throw new IllegalArgumentException("this event is not in this venue.");
			}
			if (currQty - quantity < 0) {
				throw new IllegalArgumentException("Not enough tickets left in stock !");
			}
			if (stock.replace(eventID, currQty, currQty - quantity)) {
				return;
			}
		}
	}

	protected void addEvent(int eventID) {
		stock.putIfAbsent(eventID, size);
	}

	@Override
	String getSegmentType() {
		return "F";
	}
}
