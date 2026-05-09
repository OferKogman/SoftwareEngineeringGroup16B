package com.group16b.DomainLayer.Order;

import java.util.HashMap;
import java.util.List;

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

	@Override
	public String createSeatingActiveOrder(List<String> seatIds, String segmentId, int eventID, int userID) {

		Order order = new Order(segmentId, seatIds);
		this.addOrder(order);
		return order.getOrderId();
	}

	@Override
	public String createFieldActiveOrder(int amount, String segmentId, int eventID, int userID) {
		Order order = new Order(segmentId, amount);
		this.addOrder(order);
		return order.getOrderId();	
	}
}
