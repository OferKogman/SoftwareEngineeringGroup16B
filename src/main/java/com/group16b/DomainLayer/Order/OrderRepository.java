package com.group16b.DomainLayer.Order;

import java.util.HashMap;

public class OrderRepository implements IOrderRepository {
	private final HashMap<String, Order> orders;
	private final static OrderRepository instance = new OrderRepository();

	public OrderRepository() {
		this.orders = new HashMap<>();
	}

	public static OrderRepository getInstance() {
		return instance;
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
	public boolean cancelOrder(String orderId) {
		if (this.orders.containsKey(orderId)) {
			Order order = this.orders.get(orderId);
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
