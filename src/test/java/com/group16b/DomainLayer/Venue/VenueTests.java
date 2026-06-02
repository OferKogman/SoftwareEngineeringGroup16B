package com.group16b.DomainLayer.Venue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VenueTests {

    Venue venue;
    LocalDateTime start;
    LocalDateTime end;

    @BeforeEach
    void setup() {
        venue = new Venue(
                "Test Venue",
                new Location("Test Location", "1", "Dizengoff", "Tel Aviv", null, "Israel", null, null),
                Map.of(
                        "F1", new FieldSeg("F1", 100),
                        "S1", new ChosenSeatingSeg("S1", Map.of(
                                "1-1", new Seat(1, 1),
                                "1-2", new Seat(1, 2)))),
                "venue1");
        start = LocalDateTime.now().plusDays(1);
        end = LocalDateTime.now().plusDays(2);
    }

    @Test
    void bookEvent_validEvent_addsSchedule() {
        venue.bookEvent(start, end, 1);

        assertNotNull(venue.getEventSchedule(1));
        assertEquals(start, venue.getEventSchedule(1).getStartTime());
        assertEquals(end, venue.getEventSchedule(1).getEndTime());
    }

    @Test
    void bookEvent_startAfterEnd_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> venue.bookEvent(start.plusDays(2), end, 1));
    }

    @Test
    void bookEvent_sameEventIdTwice_throwsException() {
        venue.bookEvent(start, end, 1);
        assertThrows(IllegalArgumentException.class,
                () -> venue.bookEvent(
                        start.plusDays(1),
                        end.plusDays(1),
                        1));
    }

    @Test
    void bookEvent_overlappingEvent_throwsException() {
        venue.bookEvent(start, end, 1);

        assertThrows(IllegalArgumentException.class,
                () -> venue.bookEvent(
                        start.plusHours(12),
                        end.plusHours(12),
                        2));
    }

    @Test
    void cancelEvent_existingEvent_removesSchedule() {
        venue.bookEvent(start, end, 1);
        venue.cancelEvent(start, 1);

        assertNull(venue.getEventSchedule(1));
    }

    @Test
    void cancelEvent_missingEvent_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> venue.cancelEvent(LocalDateTime.now(), 99));
    }

    @Test
    void reserveTickets_fieldSegment_reducesStock() {
        venue.bookEvent(start, end, 1);

        venue.reserveTickets("F1", 10, 1);

        FieldSeg fieldSeg = (FieldSeg) venue.getSegmentByID("F1");
        assertEquals(90, fieldSeg.getStock(1));
    }

    @Test
    void reserveTickets_missingSegment_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> venue.reserveTickets("missing", 1, 1));
    }

    @Test
    void freeTickets_fieldSegment_increasesStock() {
        venue.bookEvent(start, end, 1);

        venue.reserveTickets("F1", 10, 1);
        venue.cancelFieldReservation("F1", 10, 1);

        FieldSeg fieldSeg = (FieldSeg) venue.getSegmentByID("F1");
        assertEquals(100, fieldSeg.getStock(1));
    }

    @Test
    void freeTickets_fieldSegment_noTicketsToBeFreed() {
        venue.bookEvent(start, end, 1);
        assertThrows(IllegalArgumentException.class,
                () -> venue.cancelFieldReservation("F1", 10, 1));
    }

    @Test
    void reserveTickets_chosenSeatingSegment_reducesStock() {
        venue.bookEvent(start, end, 1);

        venue.reserveTickets("S1", List.of("1-1"), 1);

        ChosenSeatingSeg chosenSeatingSeg = (ChosenSeatingSeg) venue.getSegmentByID("S1");
        Seat s = chosenSeatingSeg.getMap().get("1-1");
        assertTrue(s.isSeatReserved(1));
    }

    @Test
    void freeTickets_chosenSeatingSegment_increasesStock() {
        venue.bookEvent(start, end, 1);

        venue.reserveTickets("S1", List.of("1-1"), 1);
        venue.cancelSeatReservation("S1", List.of("1-1"), 1);

        ChosenSeatingSeg chosenSeatingSeg = (ChosenSeatingSeg) venue.getSegmentByID("S1");
        Seat s = chosenSeatingSeg.getMap().get("1-1");
        assertFalse(s.isSeatReserved(1));
    }

    @Test
    void freeTickets_chosenSeatingSegment_noTicketsToBeFreed() {
        venue.bookEvent(start, end, 1);
        assertThrows(IllegalArgumentException.class,() -> venue.cancelSeatReservation("S1", List.of("1-1"), 1));
    }
}