package com.group16b.DomainLayer.Event;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class Venue {
	private volatile String name;
	private final String location;
	private final Map<String, Segment> segments;
	private final Map<Date, Integer> events;

	protected Venue(String name, String location, Map<String, Segment> segments) {
		this.name = name;
		this.location = location;
		this.segments = segments;
		events = new TreeMap<>();
	}

	protected String getName() {
		return name;
	}

	protected void setName(String newName) {
		name = newName;
	}

	protected String getLocation() {
		return location;
	}

	protected Segment getSegmentByID(String id) {
		return segments.get(id);
	}

	protected Integer getEventIDByDate(Date date) {
		return events.get(date);
	}

	protected void addEvent(Date date, int eventID) {
		if (events.putIfAbsent(date, eventID) != null) {
			throw new IllegalArgumentException("Venue is already reserved for requested date !");
		}
	}

	protected void removeEvent(Date date, int eventID) {
		if (!events.remove(date, eventID)) {
			throw new IllegalArgumentException("Venue is not reserved for this event at requested date !");
		}
	}
}
