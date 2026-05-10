package com.group16b.DomainLayer.Venue;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

public class Venue {
	private volatile String name;
	private final Location location;
	private final Map<String, Segment> segments;
	private final Map<LocalDateTime, Integer> events;

	protected Venue(String name, Location location, Map<String, Segment> segments) {
		this.name = name;
		this.location = location;
		this.segments = segments;
		events = new TreeMap<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String newName) {
		name = newName;
	}

	public Location getLocation() {
		return location;
	}

	public Segment getSegmentByID(String id) {
		return segments.get(id);
	}

	public Integer getEventIDByLocalDateTime(LocalDateTime date) {
		return events.get(date);
	}

	public void bookEvent(LocalDateTime startTime, LocalDateTime endTime, int eventID) {
		// initialize stock
		// use start time n end time for event
		if (events.putIfAbsent(startTime, eventID) != null) {
			throw new IllegalArgumentException("Venue is already reserved for requested date !");
		}
	}

	protected void cancelEvent(LocalDateTime date, int eventID) {
		// fix this method to use start time n end time for event
		if (!events.remove(date, eventID)) {
			throw new IllegalArgumentException("Venue is not reserved for this event at requested date !");
		}
	}

	protected void reserveSeats(ReservationRequest request) {
		Segment segment = segments.get(request.getSegmentId());
		if (segment == null) {
			throw new IllegalArgumentException("Segment with ID " + request.getSegmentId() + " not found");
		}
		segment.reserve(request);
	}
	

}
