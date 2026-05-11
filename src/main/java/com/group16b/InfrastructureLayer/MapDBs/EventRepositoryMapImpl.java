package com.group16b.InfrastructureLayer.MapDBs;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;

public class EventRepositoryMapImpl implements IEventRepository {
	private final static EventRepositoryMapImpl instance = new EventRepositoryMapImpl();
	private Map<Integer, Event> events = new TreeMap<>();

	private EventRepositoryMapImpl() {
	}

	public static EventRepositoryMapImpl getInstance() {
		return instance;
	}

	@Override
	public void addEvent(Event e) {
		if (e == null) {
			throw new IllegalArgumentException("Event cannot be null");
		}
		if (events.get(e.getEventID()) != null) {
			throw new IllegalArgumentException("Event with this ID already exists");
		}
		events.put(e.getEventID(), e);
	}

	@Override
	public void updateEvent(Event event){
		if (event == null) {
			throw new IllegalArgumentException("Event cannot be null");
		}

		if (events.replace(event.getEventID(), event) == null) {
            throw new IllegalArgumentException("Event with this ID doesn't exist");
        }	
	}

	@Override
	public Event getEventByID(int eventID) {
		Event e = events.get(eventID);
		if (e == null) {
			throw new IllegalArgumentException("Event with ID " + eventID + " not found");
		}
		return e;
	}

	@Override
	public boolean EventExists(int eventID) {
		return events.containsKey(eventID);
	}

    @Override
    public List<Event> searchEvents(List<String> name, List<String> artist, List<String> category, List<String> keyword, List<Double> minPrice, List<Double> maxPrice, List<LocalDateTime> startTime, List<LocalDateTime> endTime, List<Double> eventRating, List<Integer> productionCompanyID) {
        if(minPrice != null && !minPrice.isEmpty() && minPrice.size() != 1) {
			throw new IllegalArgumentException("Min price filter must have exactly one value.");
		}
		if(maxPrice != null && !maxPrice.isEmpty() && maxPrice.size() != 1) {
			throw new IllegalArgumentException("Max price filter must have exactly one value.");
		}
		if(startTime != null && !startTime.isEmpty() && startTime.size() != 1) {
			throw new IllegalArgumentException("Start time filter must have exactly one value.");
		}
		if(endTime != null && !endTime.isEmpty() && endTime.size() != 1) {
			throw new IllegalArgumentException("End time filter must have exactly one value.");
		}
		if(eventRating != null && !eventRating.isEmpty() && eventRating.size() != 1) {
			throw new IllegalArgumentException("Event rating filter must have exactly one value.");
		}
		return events.values().stream().filter(event -> (name == null || name.isEmpty() || name.contains(event.getEventName())) &&
				(artist == null || artist.isEmpty() || artist.contains(event.getEventArtist())) &&
				(category == null || category.isEmpty() || category.contains(event.getEventCategory())) &&
				(keyword == null || keyword.isEmpty() || keyword.stream().anyMatch(k -> event.toString().contains(k))) &&
				(minPrice == null || minPrice.isEmpty() || event.getEventPrice() >= minPrice.get(0)) &&
				(maxPrice == null || maxPrice.isEmpty() || event.getEventPrice() <= maxPrice.get(0)) &&
				(startTime == null || startTime.isEmpty() || !event.getEventEndTime().isBefore(startTime.get(0))) &&
				(endTime == null || endTime.isEmpty() || !event.getEventStartTime().isAfter(endTime.get(0))) &&
				(eventRating == null || eventRating.isEmpty() || event.getEventRating() >= eventRating.get(0)) &&
				(productionCompanyID == null || productionCompanyID.isEmpty() || productionCompanyID.contains(event.getEventProductionCompanyID()))
		).collect(Collectors.toCollection(ArrayList::new));
    }
}
