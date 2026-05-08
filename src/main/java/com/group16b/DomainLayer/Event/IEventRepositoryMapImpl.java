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
        if(e == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        if (events.get(e.getEventID())!= null) {
            throw new IllegalArgumentException("Event with this ID already exists");
        }
		events.put(e.getEventID(), e);
	}

	public Event getEventByID(int eventID) {
		Event e = events.get(eventID);
		if (e == null) {
			throw new IllegalArgumentException("Event with ID " + eventID + " not found");
		}
		return e;
	}

	public boolean EventExists(int eventID) {
		return events.containsKey(eventID);
	}
}
