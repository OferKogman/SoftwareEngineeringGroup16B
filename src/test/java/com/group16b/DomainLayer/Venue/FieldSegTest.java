package com.group16b.DomainLayer.Venue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class FieldSegTest {

    @Test
    void reserve_shouldDecreaseStock() {
        FieldSeg fieldSeg = new FieldSeg("field1", 10);
        fieldSeg.addEvent(100);

        fieldSeg.reserve(ReservationRequest.forField(100, 3, "field1"));

        assertEquals(7, fieldSeg.getStock(100));
    }

    @Test
    void cancelReservation_shouldIncreaseStock() {
        FieldSeg fieldSeg = new FieldSeg("field1", 10);
        fieldSeg.addEvent(100);

        fieldSeg.reserve(ReservationRequest.forField(100, 3, "field1"));
        fieldSeg.cancelReservation(ReservationRequest.forField(100, 3, "field1"));

        assertEquals(10, fieldSeg.getStock(100));
    }

    @Test
    void reserve_moreThanAvailableStock_shouldThrow() {
        FieldSeg fieldSeg = new FieldSeg("field1", 10);
        fieldSeg.addEvent(100);

        assertThrows(
                IllegalArgumentException.class,
                () -> fieldSeg.reserve(ReservationRequest.forField(100, 11, "field1"))
        );

        assertEquals(10, fieldSeg.getStock(100));
    }

    @Test
    void cancelReservation_moreThanFieldSize_shouldThrow() {
        FieldSeg fieldSeg = new FieldSeg("field1", 10);
        fieldSeg.addEvent(100);

        assertThrows(
                IllegalArgumentException.class,
                () -> fieldSeg.cancelReservation(ReservationRequest.forField(100, 1, "field1"))
        );
    }

    @Test
    void getSegmentType_shouldReturnF() {
        FieldSeg fieldSeg = new FieldSeg("field1", 10);

        assertEquals("F", fieldSeg.getSegmentType());
    }

    @Test
    void reserve_exactAvailableStock_shouldDepleteStockToZero() {
        FieldSeg fieldSeg = new FieldSeg("field1", 10);
        fieldSeg.addEvent(100);

        fieldSeg.reserve(ReservationRequest.forField(100, 10, "field1"));

        assertEquals(0, fieldSeg.getStock(100), "Stock should be exactly 0 after full reservation");
    }

    @Test
    void reserve_unrelatedEvent_shouldThrow() {
        FieldSeg fieldSeg = new FieldSeg("field1", 10);

        assertThrows(
                IllegalArgumentException.class,
                () -> fieldSeg.reserve(ReservationRequest.forField(999, 1, "field1")),
                "Should throw when reserving for an event that hasn't been added to the segment"
        );
    }

    @Test
    void cancelReservation_unrelatedEvent_shouldThrow() {
        FieldSeg fieldSeg = new FieldSeg("field1", 10);

        assertThrows(
                IllegalArgumentException.class,
                () -> fieldSeg.cancelReservation(ReservationRequest.forField(999, 1, "field1")),
                "Should throw when canceling for an event that hasn't been added to the segment"
        );
    }
}