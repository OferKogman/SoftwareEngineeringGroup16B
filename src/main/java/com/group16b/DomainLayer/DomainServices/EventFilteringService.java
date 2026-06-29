package com.group16b.DomainLayer.DomainServices;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.Venue.Location;
import com.group16b.DomainLayer.Venue.Venue;


@Service
@Transactional
public class EventFilteringService {

    private final IEventRepository eventRepository;
    private final IRepository<Venue> venueRepository;
    private final IProductionCompanyRepository productionCompanyPolicyRepository;

    public EventFilteringService(IProductionCompanyRepository productionCompanyPolicyRepository,
            IEventRepository eventRepo, IRepository<Venue> venueRepository) {
        this.productionCompanyPolicyRepository = productionCompanyPolicyRepository;
        this.eventRepository = eventRepo;
        this.venueRepository = venueRepository;
    }

    public List<Event> searchEvents(List<String> name, List<String> artist, List<String> category, List<String> keyword,
            List<Number> minPrice,
            List<Number> maxPrice, List<LocalDateTime> startTime, List<LocalDateTime> endTime, List<Number> eventRating,
            List<Integer> productionCompanyID, List<Location> locations, List<Number> productionCompanyRating) {
        List<Event> events = eventRepository.searchEvents(name, artist, category, keyword, minPrice, maxPrice,
                startTime, endTime, eventRating, productionCompanyID);
        events.removeIf(event -> !event.getEventStatus());
        if (locations != null && !locations.isEmpty()) {
            events.removeIf(event -> locations.stream().noneMatch(
                    location -> location.matches(venueRepository.findByID(event.getEventVenueID()).getLocation())));
        }
        if (productionCompanyID == null || productionCompanyID.isEmpty()) {
            if (productionCompanyRating != null && !productionCompanyRating.isEmpty()) {
                if (productionCompanyRating.size() != 1) {
                    throw new IllegalArgumentException("Production company rating filter must have exactly one value.");
                }
                events.removeIf(
                        event -> productionCompanyRating.get(0).doubleValue() > productionCompanyPolicyRepository
                                .findByID(String.valueOf(event.getEventProductionCompanyID())).getRating());
            }
        }
        return events;
    }
}
