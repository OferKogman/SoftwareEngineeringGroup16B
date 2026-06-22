package com.group16b.DomainLayer.Event;

import java.time.LocalDateTime;
import java.util.List;

import com.group16b.DomainLayer.Interfaces.IRepository;

public interface IEventRepository extends IRepository<Event> {

	public List<Event> searchEvents(List<String> name, List<String> artist, List<String> category, List<String> keyword,
			List<Number> minPrice,
			List<Number> maxPrice, List<LocalDateTime> startTime, List<LocalDateTime> endTime, List<Number> eventRating,
			List<Integer> productionCompanyID);
	public List<Event> findAllByVenueID(String venueID);

}
