package com.group16b.DomainLayer.Event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SeatTests {
    Seat seat;

    @BeforeEach
    void setUp() {
        seat = new Seat(1, 1);
        seat.addEvent(1);
    }

    @Test
    void SuccessfulReturnSeat() {
        seat.reserveSeat(1);
        seat.returnSeat(1);
        assertEquals(false, seat.getStock(1));
    }

    @Test
    void FailedReturnSeatInvalidEvent() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            seat.returnSeat(2);
        });
        assertEquals("this event is not in this venue.", ex.getMessage());
    }

    @Test
    void FailedReturnSeatAlreadyFree() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            seat.returnSeat(1);
        });
        assertEquals("Seat is already free !", ex.getMessage());
    }

    @Test
    void SuccessfulReserveSeat() {
        seat.reserveSeat(1);
        assertEquals(true, seat.getStock(1));
    }

    @Test
    void FailedReserveSeatInvalidEvent() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            seat.reserveSeat(2);
        });
        assertEquals("this event is not in this venue.", ex.getMessage());
    }

    @Test
    void FailedReserveSeatAlreadyReserved() {
        seat.reserveSeat(1);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            seat.reserveSeat(1);
        });
        assertEquals("Seat is already reserved !", ex.getMessage());
    }
}
