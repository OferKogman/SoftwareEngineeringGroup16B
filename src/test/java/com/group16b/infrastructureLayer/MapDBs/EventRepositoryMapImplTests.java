package com.group16b.infrastructureLayer.MapDBs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.group16b.DomainLayer.Event.Event;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;

public class EventRepositoryMapImplTests {
	static EventRepositoryMapImpl repository;
	static Event matchingEvent, wrongName, wrongArtist, wrongCategory, wrongKeyword, tooCheap, tooExpensive, tooEarly, tooLate, lowRating, wrongProductionCompany;
	static LocalDateTime now = LocalDateTime.of(2026, 5, 9, 18, 0);

	@BeforeAll
	static void setup() {
		matchingEvent = mockEvent(
            100,
            "Rock Night",
            "Queen",
            "Music",
            "live rock concert",
            100.0,
            now.plusDays(1),
            now.plusDays(2),
            4.8,
            1
		);

		wrongName = mockEvent(
				101,
				"Jazz Night",
				"Queen",
				"Music",
				"live rock concert",
				100.0,
				now.plusDays(1),
				now.plusDays(2),
				4.8,
				1
		);

		wrongArtist = mockEvent(
				102,
				"Rock Night",
				"ABBA",
				"Music",
				"live rock concert",
				100.0,
				now.plusDays(1),
				now.plusDays(2),
				4.8,
				1
		);

		wrongCategory = mockEvent(
				103,
				"Rock Night",
				"Queen",
				"Theater",
				"live rock concert",
				100.0,
				now.plusDays(1),
				now.plusDays(2),
				4.8,
				1
		);

		wrongKeyword = mockEvent(
				104,
				"Rock Night",
				"Queen",
				"Music",
				"classical orchestra",
				100.0,
				now.plusDays(1),
				now.plusDays(2),
				4.8,
				1
		);

		tooCheap = mockEvent(
				105,
				"Rock Night",
				"Queen",
				"Music",
				"live rock concert",
				20.0,
				now.plusDays(1),
				now.plusDays(2),
				4.8,
				1
		);

		tooExpensive = mockEvent(
				106,
				"Rock Night",
				"Queen",
				"Music",
				"live rock concert",
				500.0,
				now.plusDays(1),
				now.plusDays(2),
				4.8,
				1
		);

		tooEarly = mockEvent(
				107,
				"Rock Night",
				"Queen",
				"Music",
				"live rock concert",
				100.0,
				now.minusDays(2),
				now,
				4.8,
				1
		);

		tooLate = mockEvent(
				108,
				"Rock Night",
				"Queen",
				"Music",
				"live rock concert",
				100.0,
				now.plusDays(10),
				now.plusDays(12),
				4.8,
				1
		);

		lowRating = mockEvent(
				109,
				"Rock Night",
				"Queen",
				"Music",
				"live rock concert",
				100.0,
				now.plusDays(1),
				now.plusDays(2),
				2.0,
				1
		);

		wrongProductionCompany = mockEvent(
				110,
				"Rock Night",
				"Queen",
				"Music",
				"live rock concert",
				100.0,
				now.plusDays(1),
				now.plusDays(2),
				4.8,
				2
		);
		List<Event> allEvents = List.of(matchingEvent, wrongName, wrongArtist, wrongCategory, wrongKeyword, tooCheap, tooExpensive, tooEarly, tooLate, lowRating, wrongProductionCompany);
		repository = new EventRepositoryMapImpl();
		allEvents.forEach(repository::save);
	}

	@Test
	void successfulGetEventById() {
		Event retrievedEvent = repository.findByID(String.valueOf(matchingEvent.getEventID()));
		assertEquals(matchingEvent.getEventID(), retrievedEvent.getEventID());
	}

	@Test
	void FailureGetEventByIdNotFound() {
		try {
			repository.findByID("999");
		} catch (Exception e) {
			assertEquals("Event with ID 999 not found", e.getMessage());
		}
	}

	@Test
	void FailureSearchEventsMinPriceMultipleValues() {
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			repository.searchEvents(null, null, null, null, List.of(10.0, 20.0), null, null, null, null, null);
		});

		assertEquals("Min price filter must have exactly one value.", exception.getMessage());
	}

	@Test
	void FailureSearchEventsMaxPriceMultipleValues() {
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			repository.searchEvents(null, null, null, null, null, List.of(10.0, 20.0), null, null, null, null);
		});

		assertEquals("Max price filter must have exactly one value.", exception.getMessage());
	}

	@Test
	void FailureSearchEventsStartTimeMultipleValues() {
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			repository.searchEvents(null, null, null, null, null, null, List.of(LocalDateTime.now(), LocalDateTime.now().plusDays(1)), null, null, null);
		});

		assertEquals("Start time filter must have exactly one value.", exception.getMessage());
	}

	@Test
	void FailureSearchEventsEndTimeMultipleValues() {
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			repository.searchEvents(null, null, null, null, null, null, null, List.of(LocalDateTime.now(), LocalDateTime.now().plusDays(1)), null, null);
		});

		assertEquals("End time filter must have exactly one value.", exception.getMessage());
	}

	@Test
	void FailureSearchEventsEventRatingMultipleValues() {
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			repository.searchEvents(null, null, null, null, null, null, null, null, List.of(3.0, 4.0), null);
		});

		assertEquals("Event rating filter must have exactly one value.", exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("filterEventsCases")
	void filterEvents_success(
			String name,
			String artist,
			String category,
			String keyword,
			Double minPrice,
			Double maxPrice,
			LocalDateTime startTime,
			LocalDateTime endTime,
			Double eventRating,
			Integer productionCompanyID,
			List<Event> allEvents,
			List<Event> expectedEvents
	) {
		List<Event> result = repository.searchEvents(
				List.of(name),
				List.of(artist),
				List.of(category),
				List.of("Event{eventID=100, active=true, venueID='1', name='Rock Night', startTime=2026-05-10T18:00, endTime=2026-05-11T18:00, artist='Queen', category='Music', productionCompanyID=1}"),
				List.of(minPrice),
				List.of(maxPrice),
				List.of(startTime),
				List.of(endTime),
				List.of(eventRating),
				List.of(productionCompanyID)
		);
		assertEquals(1, result.size());
		assertEquals(expectedEvents.get(0).getEventID(), result.get(0).getEventID());
	}








	private static Event mockEvent(
		int eventID,
        String name,
        String artist,
        String category,
        String keyword,
        Double price,
        LocalDateTime startTime,
		LocalDateTime endTime,
        Double rating,
        Integer productionCompanyID
	) {
    Event event = mock(Event.class);

	when(event.getEventID()).thenReturn(eventID);
	when(event.getEventStatus()).thenReturn(true);
	when(event.getEventVenueID()).thenReturn("1");
    when(event.getEventName()).thenReturn(name);
    when(event.getEventStartTime()).thenReturn(startTime);
	when(event.getEventEndTime()).thenReturn(endTime);
    when(event.getEventArtist()).thenReturn(artist);
    when(event.getEventCategory()).thenReturn(category);
    when(event.getEventProductionCompanyID()).thenReturn(productionCompanyID);
	when(event.getEventDiscountPolicy()).thenReturn(new HashSet<>());
	when(event.getEventPurchasePolicy()).thenReturn(new HashSet<>());
    when(event.getEventPrice()).thenReturn(price);
    when(event.getEventRating()).thenReturn(rating);
	when(event.getOwnerId()).thenReturn(productionCompanyID);
	when(event.getVersion()).thenReturn(0L);
    when(event.toString()).thenReturn("Event{" +
				"eventID=" + eventID +
				", active=true" +
				", venueID='1'" +
				", name='" + name + '\'' +
				", startTime=" + startTime +
				", endTime=" + endTime +
				", artist='" + artist + '\'' +
				", category='" + category + '\'' +
				", productionCompanyID=" + productionCompanyID +
				'}');

    return event;
	}

	private static Stream<Arguments> filterEventsCases() {
    return Stream.of(
            Arguments.of(
                    "Rock Night",
                    "Queen",
                    "Music",
                    "rock",
                    50.0,
                    200.0,
                    now.plusDays(1),
                    now.plusDays(2),
                    4.0,
                    1,
                    List.of(
                            matchingEvent,
                            wrongName,
                            wrongArtist,
                            wrongCategory,
                            wrongKeyword,
                            tooCheap,
                            tooExpensive,
                            tooEarly,
                            tooLate,
                            lowRating,
                            wrongProductionCompany
                    ),
                    new ArrayList<>(List.of(matchingEvent))
            )
    );
}
}
