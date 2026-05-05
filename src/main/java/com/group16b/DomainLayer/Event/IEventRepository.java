package com.group16b.DomainLayer.Event;

import java.util.Map;
import java.util.TreeMap;

public class IEventRepository {
	private final static IEventRepository instance = new IEventRepository();
	private Map<Integer, Event> events = new TreeMap<>();

	private IEventRepository() {
	}

	public static IEventRepository getInstance() {
		return instance;
	}

	public void addEvent(Event e) {
		events.putIfAbsent(e.getEventID(), e);
	}

	public Event getEventByID(int eventID) {
		return events.get(eventID);
	}

	public boolean EventExists(int eventID) {
		return events.containsKey(eventID);
	}
}
