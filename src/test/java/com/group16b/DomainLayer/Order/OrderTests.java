package com.group16b.DomainLayer.Order;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;



class OrderTests {

	

    @Test
    void seatOrderCreation_shouldInitializeBasicFields() {
        List<String> seats = List.of("A-1", "A-2");

        Order order = new Order("segment1", seats, 50.0, 7, "10");

        assertEquals(OrderType.SEAT, order.getOrderType());
        assertEquals("segment1", order.getSegmentId());
        assertEquals(2, order.getNumOfTickets());
        assertEquals(50.0, order.getTotalOrderprice());
        assertEquals(7, order.getEventId());
        assertEquals("10", order.getSubjectId());
        assertEquals(seats, order.getSeats());
        assertTrue(order.isActive());
        assertFalse(order.isCompleted());
        assertEquals(0, order.getVersion());
    }

    @Test
    void fieldOrderCreation_shouldInitializeBasicFields() {
        Order order = new Order("field1", 3, 120.0, 7, "10");

        assertEquals(OrderType.FIELD, order.getOrderType());
        assertEquals("field1", order.getSegmentId());
        assertEquals(3, order.getNumOfTickets());
        assertEquals(120.0, order.getTotalOrderprice());
        assertEquals(7, order.getEventId());
        assertEquals("10", order.getSubjectId());
        assertTrue(order.isActive());
        assertFalse(order.isCompleted());
        assertEquals(0, order.getVersion());
    }

    @Test
    void fieldOrderGetSeats_shouldThrowIllegalStateException() {
        Order order = new Order("field1", 3, 100.0, 1, "1");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                order::getSeats
        );

        assertEquals(
                "This order is for field tickets, it does not have specific seats.",
                exception.getMessage()
        );
    }

    @Test
    void completeOrder_whenActive_shouldChangeStateToCompleted() {
        Order order = new Order("segment1", List.of("A1"), 100.0, 1, "1");

        boolean result = order.CompleteOrder();

        assertTrue(result);
        assertFalse(order.isActive());
        assertTrue(order.isCompleted());
    }

    @Test
    void completeOrder_whenAlreadyCompleted_shouldThrowIllegalStateException() {
        Order order = new Order("segment1", List.of("A1"), 100.0, 1, "1");
        order.CompleteOrder();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                order::CompleteOrder
        );

        assertEquals("Order is already completed.", exception.getMessage());
    }

    @Test
    void completeOrder_whenOrderExpired_shouldThrowIllegalStateException() throws Exception {
        Order order = new Order("segment1", List.of("A1"), 100.0, 1, "1");

        Field stateField = Order.class.getDeclaredField("state");
        stateField.setAccessible(true);
        Object activeState = stateField.get(order);

        Field creationTimeField = activeState.getClass().getDeclaredField("creationTime");
        creationTimeField.setAccessible(true);
        creationTimeField.setLong(activeState, System.currentTimeMillis() - (11 * 60 * 1000));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                order::CompleteOrder
        );

        assertEquals("This Order is Expired.", exception.getMessage());
    }

    @Test
    void isBelongsToSubject_sameSubject_shouldReturnTrue() {
        Order order = new Order("segment1", List.of("A1"), 50.0, 7, "10");

        assertTrue(order.isBelongsToSubject("10"));
    }

    @Test
    void isBelongsToSubject_differentSubject_shouldReturnFalse() {
        Order order = new Order("segment1", List.of("A1"), 50.0, 7, "10");

        assertFalse(order.isBelongsToSubject("wrongSubject"));
    }

    @Test
    void verifyBelongsToSubject_sameSubject_shouldNotThrow() {
        Order order = new Order("segment1", List.of("A1"), 50.0, 7, "10");

        assertDoesNotThrow(() -> order.verifyBelongsToSubject("10"));
    }

    @Test
    void verifyBelongsToSubject_differentSubject_shouldThrowIllegalArgumentException() {
        Order order = new Order("segment1", List.of("A1"), 50.0, 7, "10");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> order.verifyBelongsToSubject("wrongSubject")
        );

        assertEquals(
                "Order " + order.getOrderId() + " does not belong to subject wrongSubject",
                exception.getMessage()
        );
    }

    @Test
    void verifyTypeSeats_whenSeatOrder_shouldNotThrow() {
        Order order = new Order("segment1", List.of("A1"), 50.0, 7, "10");

        assertDoesNotThrow(order::verifyTypeSeats);
    }

    @Test
    void verifyTypeSeats_whenFieldOrder_shouldThrowIllegalStateException() {
        Order order = new Order("field1", 3, 50.0, 7, "10");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                order::verifyTypeSeats
        );

        assertEquals(
                "This order is for field tickets, it does not have specific seats.",
                exception.getMessage()
        );
    }

    @Test
    void verifyTypeField_whenFieldOrder_shouldNotThrow() {
        Order order = new Order("field1", 3, 50.0, 7, "10");

        assertDoesNotThrow(order::verifyTypeField);
    }

    @Test
    void verifyTypeField_whenSeatOrder_shouldThrowIllegalStateException() {
        Order order = new Order("segment1", List.of("A1"), 50.0, 7, "10");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                order::verifyTypeField
        );

        assertEquals(
                "This order is for seat tickets, it must have specific seats.",
                exception.getMessage()
        );
    }

    @Test
    void validateOrderIsActive_whenActive_shouldNotThrow() {
        Order order = new Order("segment1", List.of("A1"), 50.0, 7, "10");

        assertDoesNotThrow(order::validiteOrderIsActive);
    }

    @Test
    void validateOrderIsActive_whenCompleted_shouldThrowIllegalStateException() {
        Order order = new Order("segment1", List.of("A1"), 50.0, 7, "10");
        order.CompleteOrder();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                order::validiteOrderIsActive
        );

        assertEquals("Order " + order.getOrderId() + " is not active", exception.getMessage());
    }

    @Test
    void updateSeats_whenSeatOrderAndValidSeats_shouldUpdateSeatsTicketCountAndPrice() {
        Order order = new Order("segment1", List.of("A1"), 50.0, 7, "10");

        order.updateSeats(List.of("B1", "B2"), 100.0);

        assertEquals(List.of("B1", "B2"), order.getSeats());
        assertEquals(2, order.getNumOfTickets());
        assertEquals(100.0, order.getTotalOrderprice());
    }

    @Test
    void updateSeats_whenFieldOrder_shouldThrowIllegalStateException() {
        Order order = new Order("field1", 3, 50.0, 7, "10");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> order.updateSeats(List.of("A1"), 50.0)
        );

        assertEquals(
                "This order is for field tickets, it does not have specific seats.",
                exception.getMessage()
        );
    }

    @Test
    void updateSeats_whenSeatsNull_shouldThrowIllegalArgumentException() {
        Order order = new Order("segment1", List.of("A1"), 50.0, 7, "10");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> order.updateSeats(null, 50.0)
        );

        assertEquals("New seat IDs list cannot be null or empty", exception.getMessage());
    }

    @Test
    void updateSeats_whenSeatsEmpty_shouldThrowIllegalArgumentException() {
        Order order = new Order("segment1", List.of("A1"), 50.0, 7, "10");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> order.updateSeats(List.of(), 50.0)
        );

        assertEquals("New seat IDs list cannot be null or empty", exception.getMessage());
    }

    @Test
    void updateNumOfTickets_whenFieldOrderAndValidAmount_shouldUpdateAmountAndPrice() {
        Order order = new Order("field1", 3, 50.0, 7, "10");

        order.updateNumOfTickets(5, 150.0);

        assertEquals(5, order.getNumOfTickets());
        assertEquals(150.0, order.getTotalOrderprice());
    }

    @Test
    void updateNumOfTickets_whenSeatOrder_shouldThrowIllegalStateException() {
        Order order = new Order("segment1", List.of("A1"), 50.0, 7, "10");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> order.updateNumOfTickets(3, 90.0)
        );

        assertEquals(
                "This order is for seat tickets, it must have specific seats.",
                exception.getMessage()
        );
    }

    @Test
    void updateNumOfTickets_whenZero_shouldThrowIllegalArgumentException() {
        Order order = new Order("field1", 3, 50.0, 7, "10");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> order.updateNumOfTickets(0, 90.0)
        );

        assertEquals("New number of tickets must be greater than zero", exception.getMessage());
    }

    @Test
    void updateNumOfTickets_whenNegative_shouldThrowIllegalArgumentException() {
        Order order = new Order("field1", 3, 50.0, 7, "10");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> order.updateNumOfTickets(-1, 90.0)
        );

        assertEquals("New number of tickets must be greater than zero", exception.getMessage());
    }

    @Test
    void copyConstructor_shouldCopyBasicFields() {
        Order original = new Order("segment1", List.of("A1", "A2"), 100.0, 7, "10");
        original.setVersion(4);

        Order copy = new Order(original);

        assertEquals(original.getOrderId(), copy.getOrderId());
        assertEquals(original.getOrderType(), copy.getOrderType());
        assertEquals(original.getSegmentId(), copy.getSegmentId());
        assertEquals(original.getSeats(), copy.getSeats());
        assertEquals(original.getNumOfTickets(), copy.getNumOfTickets());
        assertEquals(original.getTotalOrderprice(), copy.getTotalOrderprice());
        assertEquals(original.getEventId(), copy.getEventId());
        assertEquals(original.getSubjectId(), copy.getSubjectId());
        assertEquals(original.getVersion(), copy.getVersion());
    }

    @Test
    void copyConstructor_whenOriginalChangesAfterCopy_shouldNotChangeCopy() {
        Order original = new Order("segment1", List.of("A1"), 50.0, 7, "10");
        Order copy = new Order(original);

        original.updateSeats(List.of("B1", "B2"), 100.0);

        assertEquals(List.of("A1"), copy.getSeats());
        assertEquals(1, copy.getNumOfTickets());
        assertEquals(50.0, copy.getTotalOrderprice());
    }

    @Test
    void setVersion_shouldUpdateVersion() {
        Order order = new Order("segment1", List.of("A1"), 50.0, 7, "10");

        order.setVersion(5);

        assertEquals(5, order.getVersion());
    }
}