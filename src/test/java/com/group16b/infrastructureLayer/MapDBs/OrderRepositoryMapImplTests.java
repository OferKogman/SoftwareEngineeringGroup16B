package com.group16b.infrastructureLayer.MapDBs;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

        user1OrderA = mock(Order.class);

        user1OrderB = mock(Order.class);

        user2Order = mock(Order.class);

        when(user1OrderA.getSubjectId()).thenReturn("1");
        when(user1OrderA.getOrderId()).thenReturn("order_1");

        when(user1OrderB.getSubjectId()).thenReturn("1");
        when(user1OrderB.getOrderId()).thenReturn("order_2");
        
        when(user2Order.getSubjectId()).thenReturn("2");
        when(user2Order.getOrderId()).thenReturn("order_3");

        // Use your real repository add/save/create method here.

        orderRepository.addOrder(user1OrderA);

        orderRepository.addOrder(user1OrderB);

        orderRepository.addOrder(user2Order);

    }

    @Test
    void addOrder_shouldReturnTrueAndStoreOrder() {
        OrderRepositoryMapImpl repository = new OrderRepositoryMapImpl();
        Order order = new Order("segment1", List.of("A-1"), 50.0, 7, "10");

        assertTrue(repository.addOrder(order));
        assertSame(order, repository.getOrder(order.getOrderId()));
    }

    @Test
    void addOrder_sameOrderTwice_shouldReturnFalseSecondTime() {
        OrderRepositoryMapImpl repository = new OrderRepositoryMapImpl();
        Order order = new Order("segment1", List.of("A-1"), 50.0, 7, "10");

        assertTrue(repository.addOrder(order));
        assertFalse(repository.addOrder(order));
    }

    @Test
    void cancelOrder_existingOrder_shouldRemoveAndReturnTrue() {
        OrderRepositoryMapImpl repository = new OrderRepositoryMapImpl();
        Order order = new Order("segment1", List.of("A-1"), 50.0, 7, "10");
        repository.addOrder(order);

        assertTrue(repository.cancelOrder(order.getOrderId()));
        assertNull(repository.getOrder(order.getOrderId()));
    }

    @Test
    void cancelOrder_missingOrder_shouldReturnFalse() {
        OrderRepositoryMapImpl repository = new OrderRepositoryMapImpl();

        assertFalse(repository.cancelOrder("missing_order"));
    }

    @Test
    void getOrder_missingOrder_shouldReturnNull() {
        OrderRepositoryMapImpl repository = new OrderRepositoryMapImpl();

        assertNull(repository.getOrder("missing_order"));
    }

    @Test

    void getOrdersByUserID_existingUserWithOrders_returnsOnlyThatUsersOrders() {

        List<Order> result = orderRepository.getOrdersBySubjectID("1");

        assertEquals(2, result.size());
        assertTrue(result.contains(user1OrderA));
        assertTrue(result.contains(user1OrderB));
        assertFalse(result.contains(user2Order));

    }

    @Test

    void getOrdersByUserID_userWithNoOrders_returnsEmptyList() {

        List<Order> result = orderRepository.getOrdersBySubjectID("999");

        assertNotNull(result);
        assertTrue(result.isEmpty());

    }

    @Test

    void getOrdersByUserID_doesNotReturnOrdersOfOtherUsers() {

        List<Order> result = orderRepository.getOrdersBySubjectID("2");

        assertEquals(1, result.size());
        assertTrue(result.contains(user2Order));
        assertFalse(result.contains(user1OrderA));
        assertFalse(result.contains(user1OrderB));

    }

    @Test
    void getAllCompletedOrders_returnsOnlyCompletedOrders() {
        // Assuming you have a way to mark orders as completed in your Order class
        when(user1OrderA.isActive()).thenReturn(false);
        when(user1OrderB.isActive()).thenReturn(true);
        when(user2Order.isActive()).thenReturn(false);

        List<Order> result = orderRepository.getAllCompletedOrders();

        assertEquals(2, result.size());
        assertTrue(result.contains(user1OrderA));
        assertFalse(result.contains(user1OrderB));
        assertTrue(result.contains(user2Order));
    }
}