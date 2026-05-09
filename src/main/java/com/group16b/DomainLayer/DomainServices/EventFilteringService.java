package com.group16b.DomainLayer.DomainServices;

import java.time.LocalDateTime;
import java.util.List;

import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.ProductionCompanyPolicy.IProductionCompanyPolicyRepository;
import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.DomainLayer.Venue.Location;

public class EventFilteringService {

    private final IEventRepository eventRepository;
    private final IVenueRepository venueRepository;
    private final IProductionCompanyPolicyRepository productionCompanyPolicyRepository;

    public EventFilteringService(IEventRepository eventRepository, IVenueRepository venueRepository, IProductionCompanyPolicyRepository productionCompanyPolicyRepository) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.productionCompanyPolicyRepository = productionCompanyPolicyRepository;
    }

    public List<Event> searchEvents(List<String> name, List<String> artist, List<String> category, List<String> keyword, List<Double> minPrice,
			List<Double> maxPrice, List<LocalDateTime> startTime, List<LocalDateTime> endTime, List<Double> eventRating, List<Integer> productionCompanyID, List<Location> locations, List<Double> productionCompanyRating) {
                List<Event> events = eventRepository.searchEvents(name, artist, category, keyword, minPrice, maxPrice, startTime, endTime, eventRating, productionCompanyID);
                events.removeIf(event -> !event.getEventStatus());
                if(locations != null && !locations.isEmpty()) {
                    events.removeIf(event -> locations.stream().noneMatch(location -> location.matches(venueRepository.getVenueByID(event.getEventVenueID()).getLocation())));
                }
                if(productionCompanyID == null || productionCompanyID.isEmpty()) {
                    if(productionCompanyRating != null && !productionCompanyRating.isEmpty()) {
                        if(productionCompanyRating.size() != 1) {
                            throw new IllegalArgumentException("Production company rating filter must have exactly one value.");
                        }
                        events.removeIf(event -> productionCompanyRating.get(0) > productionCompanyPolicyRepository.getProductionCompanyByID(event.getEventProductionCompanyID()).getRating());
                    }
                }
                return events;
            }
}
