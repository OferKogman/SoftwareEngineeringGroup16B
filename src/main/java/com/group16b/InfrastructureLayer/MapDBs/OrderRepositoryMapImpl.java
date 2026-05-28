package com.group16b.InfrastructureLayer.MapDBs;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.dao.OptimisticLockingFailureException;

import com.group16b.DomainLayer.Order.IOrderRepository;
import com.group16b.DomainLayer.Order.Order;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepositoryMapImpl implements IOrderRepository {
	private final ConcurrentHashMap<String, Order> orders;

	public OrderRepositoryMapImpl() {
		this.orders = new ConcurrentHashMap<>();
	}


	@Override
	public synchronized void save(Order order) {
		// INSERT
		if (!this.orders.containsKey(order.getOrderId())) {
			Order inserted = new Order(order);
			this.orders.put(order.getOrderId(), inserted);
		}
		else{
			Order current = this.orders.get(order.getOrderId());
			if(current.getVersion() != order.getVersion()) {
					throw new OptimisticLockingFailureException(
						"Order " + order.getOrderId() +
						" version mismatch. Expected " +
						order.getVersion() +
						" but found " +
						current.getVersion()
					);
				}
			Order updated = new Order(order);
			updated.setVersion(order.getVersion() + 1);
			this.orders.put(order.getOrderId(), updated);
		}
		

	}


    @Override
	public List<Order> getAll() {
		return this.orders.values().stream().map(Order::new).toList();
	} //.values().stream().filter(order -> !order.isActive()).toList(); need to cheack everywhere the assumption if active order

	@Override
	public List<Order> getBySubjectId(String subjectId) {
		return this.orders.values().stream()
				.filter(order -> order.isCompleted())
				.filter(order -> order.getSubjectId().equals(String.valueOf(subjectId)))
				.toList();
	}

	@Override
	public synchronized void delete(String orderId) {
		if (this.orders.containsKey(orderId)) {
			Order current = this.orders.get(orderId);
			if(!current.isActive()){
				throw new UnsupportedOperationException("Cannot delete a completed order");
			}
			this.orders.remove(orderId);
		}
		else{
			throw new IllegalArgumentException("Order with ID " + orderId + " not found");
		}
	}

	@Override
	public Order findByID(String orderId) {
		if(this.orders.containsKey(orderId)) {
			return new Order(this.orders.get(orderId));
		}
		else {
			throw new IllegalArgumentException("Order with ID " + orderId + " not found");
		}
	}
}
