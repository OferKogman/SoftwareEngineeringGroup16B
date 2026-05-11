package com.group16b.DomainLayer.Order;

import java.util.List;

public interface IOrderRepository {
	boolean addOrder(Order order);

	Order getOrder(String orderId);
	boolean cancelOrder(String orderId);
	List<Order> getOrdersByUserID(int userId);

}
