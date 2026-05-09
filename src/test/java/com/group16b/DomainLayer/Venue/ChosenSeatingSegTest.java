package com.group16b.DomainLayer.Venue;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ChosenSeatingSegTest {

    private ChosenSeatingSeg createSegmentWithTwoSeats(int eventID) {
        Seat seat1 = new Seat(1, 1);
        Seat seat2 = new Seat(1, 2);

        seat1.addEvent(eventID);
        seat2.addEvent(eventID);

        return new ChosenSeatingSeg(
                "S1",
                Map.of(
                        seat1.getSeatId(), seat1,
                        seat2.getSeatId(), seat2
                )
        );
    }

    @Test
    void reserveSeats_shouldReserveExistingFreeSeats() {
        int eventID = 100;
        ChosenSeatingSeg segment = createSegmentWithTwoSeats(eventID);

        segment.reserveSeats(List.of("1-1", "1-2"), eventID);

        assertTrue(segment.seats.get("1-1").isSeatReserved(eventID));
        assertTrue(segment.seats.get("1-2").isSeatReserved(eventID));
    }

    @Test
    void reserveSeats_shouldThrowWhenSeatDoesNotExist() {
        int eventID = 100;
        ChosenSeatingSeg segment = createSegmentWithTwoSeats(eventID);

        assertThrows(
                IllegalArgumentException.class,
                () -> segment.reserveSeats(List.of("1-1", "9-9"), eventID)
        );

        assertFalse(segment.seats.get("1-1").isSeatReserved(eventID));
    }

    @Test
    void reserveSeats_shouldThrowWhenSeatAlreadyReserved() {
        int eventID = 100;
        ChosenSeatingSeg segment = createSegmentWithTwoSeats(eventID);

        segment.reserveSeats(List.of("1-1"), eventID);

        assertThrows(
                IllegalArgumentException.class,
                () -> segment.reserveSeats(List.of("1-1"), eventID)
        );
    }

    @Test
    void reserveSeats_shouldThrowWhenEventDoesNotExistForSeat() {
        int eventID = 100;
        ChosenSeatingSeg segment = createSegmentWithTwoSeats(eventID);

        assertThrows(
                IllegalArgumentException.class,
                () -> segment.reserveSeats(List.of("1-1"), 999)
        );
    }

    @Test
    void reserve_shouldReserveSeatsFromReservationRequest() {
        int eventID = 100;
        ChosenSeatingSeg segment = createSegmentWithTwoSeats(eventID);

        ReservationRequest request =
                ReservationRequest.forSeats(eventID, List.of("1-1"), "S1");

        segment.reserve(request);

        assertTrue(segment.seats.get("1-1").isSeatReserved(eventID));
    }

    @Test
    void reserve_shouldReserveRequestedSeats() {
        Seat seat1 = new Seat(1, 1);
        Seat seat2 = new Seat(1, 2);
        seat1.addEvent(100);
        seat2.addEvent(100);

        Map<String, Seat> seats = new HashMap<>();
        seats.put("1-1", seat1);
        seats.put("1-2", seat2);

        ChosenSeatingSeg segment = new ChosenSeatingSeg("seg1", seats);

        segment.reserve(ReservationRequest.forSeats(100, List.of("1-1", "1-2"), "seg1"));

        assertTrue(seat1.isSeatReserved(100));
        assertTrue(seat2.isSeatReserved(100));
    }

    @Test
    void cancelReservation_shouldReturnReservedSeats() {
        Seat seat1 = new Seat(1, 1);
        seat1.addEvent(100);

        Map<String, Seat> seats = new HashMap<>();
        seats.put("1-1", seat1);

        ChosenSeatingSeg segment = new ChosenSeatingSeg("seg1", seats);

        segment.reserve(ReservationRequest.forSeats(100, List.of("1-1"), "seg1"));
        segment.cancelReservation(ReservationRequest.forSeats(100, List.of("1-1"), "seg1"));

        assertFalse(seat1.isSeatReserved(100));
    }

    @Test
    void reserve_whenOneSeatDoesNotExist_shouldRollbackAlreadyReservedSeats() {
        Seat seat1 = new Seat(1, 1);
        seat1.addEvent(100);

        Map<String, Seat> seats = new HashMap<>();
        seats.put("1-1", seat1);

        ChosenSeatingSeg segment = new ChosenSeatingSeg("seg1", seats);

        assertThrows(
                IllegalArgumentException.class,
                () -> segment.reserve(ReservationRequest.forSeats(100, List.of("1-1", "missing"), "seg1"))
        );

        assertFalse(seat1.isSeatReserved(100));
    }

    @Test
    void reserve_alreadyReservedSeat_shouldThrow() {
        Seat seat1 = new Seat(1, 1);
        seat1.addEvent(100);

        Map<String, Seat> seats = new HashMap<>();
        seats.put("1-1", seat1);

        ChosenSeatingSeg segment = new ChosenSeatingSeg("seg1", seats);

        segment.reserve(ReservationRequest.forSeats(100, List.of("1-1"), "seg1"));

        assertThrows(
                IllegalArgumentException.class,
                () -> segment.reserve(ReservationRequest.forSeats(100, List.of("1-1"), "seg1"))
        );
    }

    @Test
    void getSegmentType_shouldReturnS() {
        ChosenSeatingSeg segment = new ChosenSeatingSeg("seg1", new HashMap<>());

        assertEquals("S", segment.getSegmentType());
    }

}