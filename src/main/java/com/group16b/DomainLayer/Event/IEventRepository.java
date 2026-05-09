package com.group16b.DomainLayer.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface IEventRepository {
	// event must not be null and must have a unique ID
	public void addEvent(Event e);

	// event or null if no event with the given ID exists
	public Event getEventByID(int eventID);

	// returns true if an event with the given ID exists, false otherwise
	public boolean EventExists(int eventID);

	public List<Event> searchEvents(List<String> name, List<String> artist, List<String> category, List<String> keyword, List<Double> minPrice,
			List<Double> maxPrice, List<LocalDateTime> startTime, List<LocalDateTime> endTime, List<Double> eventRating, List<Integer> productionCompanyID);
}
