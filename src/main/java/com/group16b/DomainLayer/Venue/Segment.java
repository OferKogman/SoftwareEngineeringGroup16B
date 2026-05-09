package com.group16b.DomainLayer.Venue;

abstract class Segment {
	private final String segmentID;

	Segment(String segmentID) {
		this.segmentID = segmentID;
	}

	String getSegmentID() {
		return segmentID;
	}

	abstract void reserve(ReservationRequest request);

	abstract String getSegmentType();
}
