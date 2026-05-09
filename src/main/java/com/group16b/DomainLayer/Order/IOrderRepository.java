package com.group16b.DomainLayer.Order;

interface IOrderRepository {
	boolean addOrder(Order order);

	boolean removeOrder(String orderId);

	Order getOrder(String orderId);
}
