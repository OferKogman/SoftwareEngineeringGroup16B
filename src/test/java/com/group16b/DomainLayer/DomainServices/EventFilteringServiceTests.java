package com.group16b.DomainLayer.DomainServices;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.ProductionCompanyPolicy.IProductionCompanyPolicyRepository;
import com.group16b.DomainLayer.ProductionCompanyPolicy.ProductionCompanyPolicy;
import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.DomainLayer.Venue.Location;
import com.group16b.DomainLayer.Venue.Venue;

public class EventFilteringServiceTests {
    
     IEventRepository eventRepository = mock(IEventRepository.class);
     IVenueRepository venueRepository = mock(IVenueRepository.class);
     IProductionCompanyPolicyRepository productionCompanyPolicyRepository = mock(IProductionCompanyPolicyRepository.class);
     EventFilteringService eventFilteringService;
     List<Integer> compID = List.of(1);
     List<String> names = List.of("Rock Legends");

     Event mockEvent(int eventID, String name, String artist, String category, String keyword, double price, LocalDateTime startTime, LocalDateTime endTime, double rating, int productionCompanyID, String venueID, boolean status) {
        Event event = mock(Event.class);
        when(event.getEventID()).thenReturn(eventID);
        when(event.getEventName()).thenReturn(name);
        when(event.getEventArtist()).thenReturn(artist);
        when(event.getEventCategory()).thenReturn(category);
        when(event.toString()).thenReturn(keyword);
        when(event.getEventPrice()).thenReturn(price);
        when(event.getEventStartTime()).thenReturn(startTime);
        when(event.getEventEndTime()).thenReturn(endTime);
        when(event.getEventRating()).thenReturn(rating);
        when(event.getEventProductionCompanyID()).thenReturn(productionCompanyID);
        when(event.getEventVenueID()).thenReturn(venueID);
        when(event.getEventStatus()).thenReturn(status);
        return event;
    }

     Event lowRatingEvent, wrongCompanyEvent, wrongVenueEvent, inactiveEvent;
     LocalDateTime now = LocalDateTime.of(2026, 5, 9, 18, 0);
    
    @BeforeEach
     void setup() {
        lowRatingEvent = mockEvent(
            101,
            "Jazz Evening",
            "Miles Davis",
            "Music",
            "jazz performance",
            150.0,
            now.plusDays(3),
            now.plusDays(4),
            3,
            1,
            "1",
            true
        );
        wrongCompanyEvent = mockEvent(
            102,
            "Pop Stars",
            "Taylor Swift",
            "Music",
            "pop concert",
            200.0,
            now.plusDays(5),
            now.plusDays(6),
            4.5,
            2,
            "2",
            true
        );
        wrongVenueEvent = mockEvent(
            103,
            "Classical Night",
            "Yo-Yo Ma",
            "Music",
            "classical performance",
            120.0,
            now.plusDays(7),
            now.plusDays(8),
            4.8,
            1,
            "1",
            true
        );
        inactiveEvent = mockEvent(
            104,
            "Rock Legends",
            "Led Zeppelin",
            "Music",
            "rock concert",
            180.0,
            now.plusDays(9),
            now.plusDays(10),
            4.7,
            2,
            "2",
            false
        );
        when(eventRepository.searchEvents(
            not(same(names)), any(), any(), any(), any(), any(), any(), any(), any(), same(compID)
        )).thenReturn(new ArrayList<>(List.of(lowRatingEvent, wrongVenueEvent, inactiveEvent)));
        when(eventRepository.searchEvents(
            not(same(names)), any(), any(), any(), any(), any(), any(), any(), any(), not(same(compID))
        )).thenReturn(new ArrayList<>(List.of(lowRatingEvent, wrongCompanyEvent, wrongVenueEvent, inactiveEvent)));
        when(eventRepository.searchEvents(
            same(names), any(), any(), any(), any(), any(), any(), any(), any(), not(same(compID))
        )).thenReturn(new ArrayList<>(List.of(inactiveEvent)));
        Venue v1 = mock(Venue.class);
        when(v1.getLocation()).thenReturn(new Location("A", "A", "A", "A", "A", "A", 0.0, 0.0));
        when(venueRepository.getVenueByID("1")).thenReturn(v1);
        Venue v2 = mock(Venue.class);
        when(v2.getLocation()).thenReturn(new Location("B", "B", "B", "B", "B", "B", 0.0, 0.0));
        when(venueRepository.getVenueByID("2")).thenReturn(v2);
        ProductionCompanyPolicy pcp1 = mock(ProductionCompanyPolicy.class);
        when(pcp1.getRating()).thenReturn(3.0);
        ProductionCompanyPolicy pcp2 = mock(ProductionCompanyPolicy.class);
        when(pcp2.getRating()).thenReturn(5.0);
        when(productionCompanyPolicyRepository.getProductionCompanyByID(1)).thenReturn(pcp1);
        when(productionCompanyPolicyRepository.getProductionCompanyByID(2)).thenReturn(pcp2);
        eventFilteringService = new EventFilteringService();
    }

    @Test
    void SuccessfulSearchEventsCompanyIDOverridesRating() {
        List<Event> results = eventFilteringService.searchEvents(
            null, null, null, null, null, null, null, null, null, compID, null, List.of(4.0)
        );
        assertEquals(List.of(lowRatingEvent, wrongVenueEvent), results);
    }

    @Test
    void SuccessfulSearchEventsNoCompanyIDFiltersByRating() {
        List<Event> results = eventFilteringService.searchEvents(
            null, null, null, null, null, null, null, null, null, null, null, List.of(4.0)
        );
        assertEquals(List.of(wrongCompanyEvent), results);
    }

    @Test
    void FailureSearchEventsInvalidRatingSize() {
        try {
            eventFilteringService.searchEvents(
                null, null, null, null, null, null, null, null, null, null, null, List.of(4.0, 5.0)
            );
        }
        catch (IllegalArgumentException e) {
            assertEquals("Production company rating filter must have exactly one value.", e.getMessage());
        }
    }

    @Test
    void FailureSearchEventsInactive(){
        List<Event> results = eventFilteringService.searchEvents(
            names, null, null, null, null, null, null, null, null, null, null, null
        );
        assertEquals(List.of(), results);
    }

    @Test 
    void SuccessfulSearchEventsLocationFilter() {
        List<Location> locations = List.of(new Location("A", "A", "A", "A", "A", "A", 0.0, 0.0));
        List<Event> results = eventFilteringService.searchEvents(
            null, null, null, null, null, null, null, null, null, null, locations, null
        );
        assertEquals(List.of(lowRatingEvent, wrongVenueEvent), results);
    }
}
