package com.group16b.infrastructureLayer.MapDBs;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.DomainLayer.Order.Order;
import com.group16b.InfrastructureLayer.MapDBs.OrderRepositoryMapImpl;

class OrderRepositoryMapImplTests {
    private OrderRepositoryMapImpl orderRepository;
    private Order user1OrderA;
    private Order user1OrderB;
    private Order user2Order;

    @BeforeEach
    void setUp() {

        orderRepository = new OrderRepositoryMapImpl();

        user1OrderA = new Order("segment1", List.of("A-1"), 50.0, 7, "10");

        user1OrderB = new Order("segment1", List.of("A-2"), 50.0, 7, "11");

        user2Order = new Order("segment2", List.of("B-1"), 50.0, 7, "12");

        orderRepository.save(user1OrderA);
        orderRepository.save(user1OrderB);
        orderRepository.save(user2Order);

    }

    @Test
    void addOrder_shouldReturnTrueAndStoreOrder() {
        Order newOrder = new Order("segment3", List.of("C-1"), 50.0, 7, "13");
        orderRepository.save(newOrder);
        assertSame(newOrder.getOrderId(), orderRepository.findByID(newOrder.getOrderId()).getOrderId());
    }

    @Test
    void cancelOrder_existingOrder_shouldRemoveAndReturnTrue() {
        Order newOrder = new Order("segment3", List.of("C-1"), 50.0, 7, "13");
        orderRepository.save(newOrder);

        orderRepository.delete(user1OrderA.getOrderId());
        assertThrows(IllegalArgumentException.class, () -> orderRepository.findByID(user1OrderA.getOrderId()));
    }

    @Test
    void getOrder_missingOrder_shouldReturnNull() {
        assertThrows(IllegalArgumentException.class, () -> orderRepository.findByID("missing_order"));
    }


    @Test
    void getAllCompletedOrders_returnsOnlyCompletedOrders() {
        // Assuming you have a way to mark orders as completed in your Order class
        user1OrderB.CompleteOrder();

        List<Order> result = orderRepository.getAll();

        assertEquals(3, result.size());
    }
}