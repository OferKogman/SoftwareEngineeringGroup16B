package com.group16b.InfrastructureLayer.MapDBs;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.dao.OptimisticLockingFailureException;

import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;

public class EventRepositoryMapImpl implements IEventRepository {
	private Map<Integer, Event> events = new TreeMap<>();


	public EventRepositoryMapImpl() {
	}

	@Override
	public synchronized void save(Event e) {
		if (e == null) {
			throw new IllegalArgumentException("Event cannot be null");
		}

		Event curr = events.get(e.getEventID());

		if (curr != null) {
			if (curr.getVersion() != e.getVersion()){
				throw new OptimisticLockingFailureException(
                    "Event " + e.getEventID() + " version mismatch. Expected " +
                    e.getVersion() +
                    " but found " +
                    curr.getVersion()
                );
			}
			e.incrementVersion();
			events.put(e.getEventID(), e);
		}else {
			events.put(e.getEventID(), e);
		}

	}

	@Override
	public Event findByID(String eventID) {
		int id = parseID(eventID);
		Event e = events.get(id);
		if (e == null) {
			throw new IllegalArgumentException("Event with ID " + eventID + " not found");
		}
		return new Event(e);
	}

	@Override
	public List<Event> getAll() {
		return events.values().stream().map(Event::new).collect(Collectors.toCollection(ArrayList::new));
	}

	@Override
	public void delete(String eventID) {
		throw new UnsupportedOperationException("Delete operation is not supported for EventRepositoryMapImpl");
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
		).map(e -> new Event(e)).collect(Collectors.toCollection(ArrayList::new));
    }

	private int parseID(String ID)
    {
        try {
            return Integer.parseInt(ID);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid company ID: " + ID);
        }
    }
}
