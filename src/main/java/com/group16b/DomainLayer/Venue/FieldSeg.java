package com.group16b.DomainLayer.Venue;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Map;
import com.group16b.ApplicationLayer.DTOs.FieldSegDTO;

public class FieldSeg extends Segment {
	private final int size;

	private ConcurrentMap<Integer, Integer> stock;

	public FieldSeg(String segID, int size) {
		super(segID);
		this.size = size;
		stock = new ConcurrentHashMap<Integer, Integer>();
	}

	public FieldSeg(FieldSegDTO fieldSegDTO, String segmentID){
		super(segmentID);
		this.size = fieldSegDTO.getSize();

		this.stock = new ConcurrentHashMap<>();
		stock.putAll(fieldSegDTO.getStocks());

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

	@Override
	public void setStockForEvent(int eventID, int newStock){
		if(!this.stock.containsKey(eventID)){
			throw new IllegalArgumentException("cannot modify segments unrelated to event");	
		}
		stock.put(eventID, newStock);//maybe add a maximum number?
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
	public Map<Integer, Integer> getMap(){
		return stock;
	}
}
