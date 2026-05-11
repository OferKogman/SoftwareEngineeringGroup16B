package com.group16b.DomainLayer.Order;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompletedOrderTests {

    @Test
    void isActive_shouldReturnFalse() {
        CompletedOrder completedOrder = new CompletedOrder();

        assertFalse(completedOrder.isActive());
    }

    @Test
    void completeOrder_shouldThrowException() {
        CompletedOrder completedOrder = new CompletedOrder();

        assertThrows(IllegalStateException.class, completedOrder::completeOrder);
    }

    @Test
    void setTicketsAndGetTickets_shouldReturnTickets() {
        CompletedOrder completedOrder = new CompletedOrder();
        List<String> tickets = List.of("ticket1", "ticket2");

        completedOrder.setTickets(tickets);

        assertEquals(tickets, completedOrder.getTickets());
    }

    @Test
    void setTickets_shouldCopyList() {
        CompletedOrder completedOrder = new CompletedOrder();
        List<String> tickets = new java.util.ArrayList<>();
        tickets.add("ticket1");

        completedOrder.setTickets(tickets);
        tickets.add("ticket2");

        assertEquals(List.of("ticket1"), completedOrder.getTickets());
    }
}