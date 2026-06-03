package com.group16b.DomainLayer.Venue;

import java.util.Map;

abstract public class Segment {
	protected final String segmentID;
	protected GridRectangle area;

	public Segment(String segmentID, GridRectangle area) {
		this.segmentID = segmentID;
		this.area = area;
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
