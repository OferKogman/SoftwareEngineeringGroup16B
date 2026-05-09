package com.group16b.DomainLayer.Order;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class ActiveOrderTest {
    @Test
    void completeOrder_shouldReturnCompletedOrderWhenOrderIsNotExpired() {
        ActiveOrder activeOrder = new ActiveOrder();

        CompletedOrder completedOrder = activeOrder.completeOrder();

        assertNotNull(completedOrder);
        assertEquals(List.of(), completedOrder.getTickets());
    }

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
}