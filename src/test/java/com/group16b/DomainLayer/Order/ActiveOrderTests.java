package com.group16b.DomainLayer.Order;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;
public class ActiveOrderTests {

    @Test
    void getTickets_shouldThrowWhenOrderIsStillActive() {
        ActiveOrder activeOrder = new ActiveOrder();

        assertThrows(
                IllegalStateException.class,
                activeOrder::getTickets
        );
    }

    @Test
    void completeOrder_shouldThrowWhenOrderIsExpired() throws Exception {
        ActiveOrder activeOrder = new ActiveOrder();

        Field creationTimeField = ActiveOrder.class.getDeclaredField("creationTime");
        creationTimeField.setAccessible(true);

        long elevenMinutesAgo = System.currentTimeMillis() - (11 * 60 * 1000);
        creationTimeField.setLong(activeOrder, elevenMinutesAgo);

        assertThrows(
                IllegalStateException.class,
                activeOrder::completeOrder
        );
    }

    @Test
    void isActive_shouldReturnTrue() {
        ActiveOrder activeOrder = new ActiveOrder();

        assertTrue(activeOrder.isActive());
    }

    @Test
    void getTickets_shouldThrowException() {
        ActiveOrder activeOrder = new ActiveOrder();

        assertThrows(IllegalStateException.class, activeOrder::getTickets);
    }

    @Test
    void setTickets_shouldThrowException() {
        ActiveOrder activeOrder = new ActiveOrder();

        assertThrows(
                IllegalStateException.class,
                () -> activeOrder.setTickets(List.of("ticket1"))
        );
    }

    @Test
    void completeOrder_shouldReturnCompletedOrder() {
        ActiveOrder activeOrder = new ActiveOrder();

        CompletedOrder completedOrder = activeOrder.completeOrder();

        assertNotNull(completedOrder);
        assertFalse(completedOrder.isActive());
    }

}