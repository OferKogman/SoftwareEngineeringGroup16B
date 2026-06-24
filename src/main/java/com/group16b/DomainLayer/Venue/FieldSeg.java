package com.group16b.DomainLayer.Venue;

import java.util.HashMap;
import java.util.Map;

import com.group16b.ApplicationLayer.DTOs.FieldSegDTO;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "field_segments")
public class FieldSeg extends Segment {
	private int size;

    @ElementCollection
    @CollectionTable(name = "field_seg_stock", joinColumns = @JoinColumn(name = "segmentID"))
    @MapKeyColumn(name = "stock_key")
    @Column(name = "stock_value")
	private Map<Integer, Integer> stock;

	public FieldSeg(String segID, int size, GridRectangle area) {
		super(segID, area);
		this.size = size;
		stock = new HashMap<Integer, Integer>();
	}

	public FieldSeg() {
		// Default constructor for JPA
		this.size = 0;
		this.stock = new HashMap<Integer, Integer>();
	}
	
	public FieldSeg(FieldSeg other) {
		super(other);
		this.size = other.size;
		this.stock = new HashMap<>(other.stock);
	}
	public int getFieldSize() {
		return size;
	}

	public int getStock(int eventID) {
		return stock.get(eventID);
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


	public void setStockForEvent(int eventID, int newStock) {
		if (!this.stock.containsKey(eventID)) {
			throw new IllegalArgumentException("cannot modify segments unrelated to event");
		}
		stock.put(eventID, newStock);// maybe add a maximum number?
	}

	protected void setSize(int newSize) {
		if (newSize < 0) {
			throw new IllegalArgumentException("Size cannot be negative");
		}
		this.size = newSize;
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
