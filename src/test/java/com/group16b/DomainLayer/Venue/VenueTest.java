package com.group16b.DomainLayer.Venue;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VenueTest {

    @Test
    void getSegmentByID_existingSegment_shouldReturnSegment() {
        FieldSeg fieldSeg = new FieldSeg("field1", 100);
        Map<String, Segment> segments = new HashMap<>();
        segments.put("field1", fieldSeg);

        Venue venue = new Venue("venue1", "Beer Sheva", segments);

        assertSame(fieldSeg, venue.getSegmentByID("field1"));
    }

    @Test
    void getSegmentByID_missingSegment_shouldReturnNull() {
        Venue venue = new Venue("venue1", "Beer Sheva", new HashMap<>());

        assertNull(venue.getSegmentByID("missing"));
    }

    @Test
    void reserveSeats_forFieldSegment_shouldDecreaseStock() {
        FieldSeg fieldSeg = new FieldSeg("field1", 100);
        fieldSeg.addEvent(200);

        Map<String, Segment> segments = new HashMap<>();
        segments.put("field1", fieldSeg);

        Venue venue = new Venue("venue1", "Beer Sheva", segments);

        venue.reserveSeats(ReservationRequest.forField(200, 5, "field1"));

        assertEquals(95, fieldSeg.getStock(200));
    }

    @Test
    void reserveSeats_forChosenSeatingSegment_shouldReserveSeats() {
        Seat seat = new Seat(1, 1);
        seat.addEvent(200);

        Map<String, Seat> seats = new HashMap<>();
        seats.put("1-1", seat);

        ChosenSeatingSeg seatingSeg = new ChosenSeatingSeg("seg1", seats);

        Map<String, Segment> segments = new HashMap<>();
        segments.put("seg1", seatingSeg);

        Venue venue = new Venue("venue1", "Beer Sheva", segments);

        venue.reserveSeats(ReservationRequest.forSeats(200, List.of("1-1"), "seg1"));

        assertTrue(seat.isSeatReserved(200));
    }

    @Test
    void reserveSeats_missingSegment_shouldThrow() {
        Venue venue = new Venue("venue1", "Beer Sheva", new HashMap<>());

        assertThrows(
                IllegalArgumentException.class,
                () -> venue.reserveSeats(ReservationRequest.forField(200, 5, "missing"))
        );
    }

    @Test
    void bookEvent_sameStartTimeTwice_shouldThrow() {
        Venue venue = new Venue("venue1", "Beer Sheva", new HashMap<>());
        LocalDateTime start = LocalDateTime.of(2026, 5, 9, 20, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 9, 22, 0);

        venue.bookEvent(start, end, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> venue.bookEvent(start, end, 2)
        );
    }
}
