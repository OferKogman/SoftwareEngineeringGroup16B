package com.group16b.DomainLayer.Event;

import java.util.Map;
import java.util.TreeMap;

class IEventRepository {
	private final static IEventRepository instance = new IEventRepository();
	private Map<Integer, Event> events = new TreeMap<>();

	private IEventRepository() {
	}

	protected IEventRepository getInstance() {
		return instance;
	}

	protected void addEvent(Event e) {
		events.putIfAbsent(e.getEventID(), e);
	}

	protected Event getEventByID(int eventID) {
		return events.get(eventID);
	}

	protected boolean EventExists(int eventID) {
		return events.containsKey(eventID);
	}
}
