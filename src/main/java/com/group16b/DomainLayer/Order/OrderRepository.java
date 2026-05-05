package com.group16b.DomainLayer.Order;

import java.util.HashMap;

public class OrderRepository implements IOrderRepository {
    private HashMap<String, Order> orders;

    public OrderRepository() {
        this.orders = new HashMap<>();
    }

    @Override
    public boolean addOrder(Order order) {
        if (this.orders.containsKey(order.getOrderId())) {
            return false;
        }
        this.orders.put(order.getOrderId(), order);
        return true;
    }

    @Override
    public boolean removeOrder(String orderId) {
        if (this.orders.containsKey(orderId)) {
            this.orders.remove(orderId);
            return true;
        }
        return false;
    }

    @Override
    public Order getOrder(String orderId) {
        return this.orders.get(orderId);
    }
}
