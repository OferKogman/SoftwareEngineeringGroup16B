package com.group16b.DomainLayer.Order;

interface IOrderRepository {
	boolean addOrder(Order order);

	Order getOrder(String orderId);
	boolean cancelOrder(String orderId);

}
