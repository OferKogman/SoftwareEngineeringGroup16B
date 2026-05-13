package com.group16b.DomainLayer.Venue;

import java.util.Map;

abstract public class Segment {
	protected final String segmentID;

	public Segment(String segmentID) {
		this.segmentID = segmentID;
	}

	public String getSegmentID() {
		return segmentID;
	}

	public abstract void reserve(ReservationRequest request);
	public abstract void cancelReservation(ReservationRequest request);

	public abstract String getSegmentType();
	public abstract double getPrice(int eventID);
	public abstract void setStockForEvent(int eventID, int stock);
	public abstract Map<?, ?> getMap();
}
