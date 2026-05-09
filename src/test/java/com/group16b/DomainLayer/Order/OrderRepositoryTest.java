package com.group16b.DomainLayer.Order;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderRepositoryTest {

    @Test
    void addOrder_shouldReturnTrueAndStoreOrder() {
        OrderRepository repository = new OrderRepository();
        Order order = new Order("segment1", List.of("A-1"), "token123", 50.0, 7, 10);

        assertTrue(repository.addOrder(order));
        assertSame(order, repository.getOrder(order.getOrderId()));
    }

    @Test
    void addOrder_sameOrderTwice_shouldReturnFalseSecondTime() {
        OrderRepository repository = new OrderRepository();
        Order order = new Order("segment1", List.of("A-1"), "token123", 50.0, 7, 10);

        assertTrue(repository.addOrder(order));
        assertFalse(repository.addOrder(order));
    }

    @Test
    void cancelOrder_existingOrder_shouldRemoveAndReturnTrue() {
        OrderRepository repository = new OrderRepository();
        Order order = new Order("segment1", List.of("A-1"), "token123", 50.0, 7, 10);
        repository.addOrder(order);

        assertTrue(repository.cancelOrder(order.getOrderId()));
        assertNull(repository.getOrder(order.getOrderId()));
    }

    @Test
    void cancelOrder_missingOrder_shouldReturnFalse() {
        OrderRepository repository = new OrderRepository();

        assertFalse(repository.cancelOrder("missing_order"));
    }

    @Test
    void getOrder_missingOrder_shouldReturnNull() {
        OrderRepository repository = new OrderRepository();

        assertNull(repository.getOrder("missing_order"));
    }
}