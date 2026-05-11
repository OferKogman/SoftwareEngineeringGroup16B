package com.group16b.InfrastructureLayer.MapDBs;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.group16b.DomainLayer.Order.IOrderRepository;
import com.group16b.DomainLayer.Order.Order;

public class OrderRepositoryMapImpl implements IOrderRepository {
	private final ConcurrentHashMap<String, Order> orders;
	private final static OrderRepositoryMapImpl instance = new OrderRepositoryMapImpl();

	public OrderRepositoryMapImpl() {
		this.orders = new ConcurrentHashMap<>();
	}

	public static OrderRepositoryMapImpl getInstance() {
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
	public List<Order> getAllCompletedOrders() {
		return this.orders.values().stream()
				.filter(order -> !order.isActive())
				.toList();
	}

	

	@Override
	public boolean cancelOrder(String orderId) {
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
	public List<Order> getOrdersBySubjectID(String subjectID) {
		return this.orders.values().stream()
				.filter(order -> order.getSubjectId().equals(subjectID))
				.toList();
	}


}
