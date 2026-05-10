package com.group16b.DomainLayer.Venue;

abstract public class Segment {
	private final String segmentID;

	Segment(String segmentID) {
		this.segmentID = segmentID;
	}

	public String getSegmentID() {
		return segmentID;
	}

	public abstract void reserve(ReservationRequest request);
	public abstract void cancelReservation(ReservationRequest request);

	public abstract String getSegmentType();
	public abstract double getPrice(int eventID);
}
