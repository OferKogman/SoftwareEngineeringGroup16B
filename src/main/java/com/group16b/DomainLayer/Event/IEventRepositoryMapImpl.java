package com.group16b.DomainLayer.Event;

import java.util.Map;
import java.util.TreeMap;

public class IEventRepositoryMapImpl implements IEventRepository {
    private final static IEventRepositoryMapImpl instance = new IEventRepositoryMapImpl();
	private Map<Integer, Event> events = new TreeMap<>();

	private IEventRepositoryMapImpl() {
	}

	public static IEventRepositoryMapImpl getInstance() {
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
