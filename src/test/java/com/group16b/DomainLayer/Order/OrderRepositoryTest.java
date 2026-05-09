package com.group16b.DomainLayer.Order;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class OrderRepositoryTest {

    @Test
    void createSeatingActiveOrder_shouldCreateStoreAndReturnOrderId() {
        OrderRepository repository = new OrderRepository();

        String orderId = repository.createSeatingActiveOrder(
                List.of("1-1", "1-2"),
                "segment_1",
                100,
                5
        );

        Order order = repository.getOrder(orderId);

        assertNotNull(order);
        assertEquals(orderId, order.getOrderId());
        assertEquals("segment_1", order.getSegmentId());
        assertEquals(OrderType.SEAT, order.getOrderType());
        assertEquals(List.of("1-1", "1-2"), order.getSeats());
        assertEquals(2, order.getNumOfTickets());
        assertTrue(order.getState() instanceof ActiveOrder);
    }

    @Test
    void createFieldActiveOrder_shouldCreateStoreAndReturnOrderId() {
        OrderRepository repository = new OrderRepository();

        String orderId = repository.createFieldActiveOrder(
                3,
                "field_segment_1",
                100,
                5
        );

        Order order = repository.getOrder(orderId);

        assertNotNull(order);
        assertEquals(orderId, order.getOrderId());
        assertEquals("field_segment_1", order.getSegmentId());
        assertEquals(OrderType.FIELD, order.getOrderType());
        assertEquals(3, order.getNumOfTickets());
        assertTrue(order.getState() instanceof ActiveOrder);
    }

    @Test
    void createFieldActiveOrder_getSeatsShouldThrow() {
        OrderRepository repository = new OrderRepository();

        String orderId = repository.createFieldActiveOrder(
                3,
                "field_segment_1",
                100,
                5
        );

        Order order = repository.getOrder(orderId);

        assertThrows(
                IllegalStateException.class,
                order::getSeats
        );
    }
}