package com.group16b.DomainLayer.Venue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.ApplicationLayer.Records.ChosenSeatingSegRecord;
import com.group16b.ApplicationLayer.Records.EntranceRecord;
import com.group16b.ApplicationLayer.Records.FieldSegRecord;
import com.group16b.ApplicationLayer.Records.GridRectangleRecord;
import com.group16b.ApplicationLayer.Records.SeatRecord;
import com.group16b.ApplicationLayer.Records.StageRecord;

class VenueTests {

    Venue venue;
    LocalDateTime start;
    LocalDateTime end;

    @BeforeEach
    void setup() {
        venue = new Venue(
                "Test Venue",
                new Location("Test Location", "1", "Dizengoff", "Tel Aviv", null, "Israel", null, null),
                new ConcurrentHashMap<>(Map.of(
                        "F1", new FieldSeg("F1", 100, new GridRectangle(1, 2, 3, 4)),
                        "S1", new ChosenSeatingSeg("S1", new ConcurrentHashMap<>(Map.of(
                                "1-1", new Seat(1, 1),
                                "1-2", new Seat(1, 2)
                        )), new GridRectangle(5, 6, 7, 8))
                )),
                "venue1",
                new VenueGrid(6, 7),
                new ConcurrentHashMap<>(),
                new ConcurrentHashMap<>(),
                1
        );

        start = LocalDateTime.now().plusDays(1);
        end = start.plusDays(1);
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
    @Test
    void removeSegment_existingSegment_removesSegment() {
        venue.removeSegment("F1");

        assertFalse(venue.hasSegment("F1"));
    }

    @Test
    void removeSegment_missingSegment_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> venue.removeSegment("missing"));
    }

    @Test
    void replaceStages_replacesOldStages() {
        venue.replaceStages(List.of(
                new StageRecord("ST1", new GridRectangleRecord(1, 1, 2, 3)),
                new StageRecord("ST2", new GridRectangleRecord(4, 4, 1, 1))
        ));

        assertEquals(2, venue.getStages().size());
        assertTrue(venue.getStages().containsKey("ST1"));
        assertTrue(venue.getStages().containsKey("ST2"));

        venue.replaceStages(List.of(
                new StageRecord("ST3", new GridRectangleRecord(0, 0, 1, 1))
        ));

        assertEquals(1, venue.getStages().size());
        assertFalse(venue.getStages().containsKey("ST1"));
        assertFalse(venue.getStages().containsKey("ST2"));
        assertTrue(venue.getStages().containsKey("ST3"));
    }

    @Test
    void replaceEntrances_replacesOldEntrances() {
        venue.replaceEntrances(List.of(
                new EntranceRecord("EN1", new GridRectangleRecord(1, 1, 1, 1)),
                new EntranceRecord("EN2", new GridRectangleRecord(2, 2, 1, 1))
        ));

        assertEquals(2, venue.getEntrances().size());
        assertTrue(venue.getEntrances().containsKey("EN1"));
        assertTrue(venue.getEntrances().containsKey("EN2"));

        venue.replaceEntrances(List.of(
                new EntranceRecord("EN3", new GridRectangleRecord(3, 3, 1, 1))
        ));

        assertEquals(1, venue.getEntrances().size());
        assertFalse(venue.getEntrances().containsKey("EN1"));
        assertFalse(venue.getEntrances().containsKey("EN2"));
        assertTrue(venue.getEntrances().containsKey("EN3"));
    }

    @Test
    void initializeSegmentForEvent_fieldSegment_initializesStock() {
        venue.addFieldSegment(
                new FieldSegRecord("F2", 50, new GridRectangleRecord(0, 0, 1, 1))
        );

        venue.initializeSegmentForEvent("F2", 1);

        FieldSeg fieldSeg = (FieldSeg) venue.getSegmentByID("F2");
        assertEquals(50, fieldSeg.getStock(1));

        venue.reserveTickets("F2", 10, 1);
        assertEquals(40, fieldSeg.getStock(1));
    }

    @Test
    void initializeSegmentForEvent_seatingSegment_initializesSeats() {
        venue.addChosenSeatingSegment(
                new ChosenSeatingSegRecord(
                        "S2",
                        List.of(new SeatRecord(1, 1), new SeatRecord(1, 2)),
                        new GridRectangleRecord(0, 0, 1, 2)
                )
        );

        venue.initializeSegmentForEvent("S2", 1);

        venue.reserveTickets("S2", List.of("1-1"), 1);

        ChosenSeatingSeg seatingSeg = (ChosenSeatingSeg) venue.getSegmentByID("S2");
        assertTrue(seatingSeg.getMap().get("1-1").isSeatReserved(1));
        assertFalse(seatingSeg.getMap().get("1-2").isSeatReserved(1));
    }

    @Test
    void initializeSegmentForEvent_missingSegment_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> venue.initializeSegmentForEvent("missing", 1));
    }

    @Test
    void setNewSeatingStock_addsAndRemovesSeats() {
        venue.bookEvent(start, end, 1);

        venue.setNewSeatingStock(
                "S1",
                List.of("1-1", "1-3"),
                List.of(1)
        );

        ChosenSeatingSeg seatingSeg = (ChosenSeatingSeg) venue.getSegmentByID("S1");

        assertTrue(seatingSeg.getMap().containsKey("1-1"));
        assertFalse(seatingSeg.getMap().containsKey("1-2"));
        assertTrue(seatingSeg.getMap().containsKey("1-3"));

        venue.reserveTickets("S1", List.of("1-3"), 1);
        assertTrue(seatingSeg.getMap().get("1-3").isSeatReserved(1));
    }

    @Test
    void setNewSeatingStock_missingSegment_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> venue.setNewSeatingStock("missing", List.of("1-1"), List.of(1)));
    }

    @Test
    void setNewSeatingStock_fieldSegment_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> venue.setNewSeatingStock("F1", List.of("1-1"), List.of(1)));
    }
}