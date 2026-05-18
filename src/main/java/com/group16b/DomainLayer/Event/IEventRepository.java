package com.group16b.DomainLayer.Event;

import java.time.LocalDateTime;
import java.util.List;

import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.VirtualQueue.VirtualQueue;

public interface IEventRepository extends IRepository<Event> {

	public List<Event> searchEvents(List<String> name, List<String> artist, List<String> category, List<String> keyword, List<Double> minPrice,
			List<Double> maxPrice, List<LocalDateTime> startTime, List<LocalDateTime> endTime, List<Double> eventRating, List<Integer> productionCompanyID);
	
}
