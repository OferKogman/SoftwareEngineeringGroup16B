package com.group16b.DomainLayer.Order;

import java.util.List;

interface IOrderRepository {
	boolean addOrder(Order order);

	boolean removeOrder(String orderId);

	Order getOrder(String orderId);

	String createSeatingActiveOrder(List<String> seatIds, String segmentId, int eventID, int userID);
	String createFieldActiveOrder(int amount, String segmentId, int eventID, int userID);
}
