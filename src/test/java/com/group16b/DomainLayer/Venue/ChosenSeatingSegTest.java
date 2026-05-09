package com.group16b.DomainLayer.Venue;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
}