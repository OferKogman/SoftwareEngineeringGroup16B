package com.group16b.DomainLayer.Order;

import java.util.List;

interface OrderState {

	List<Ticket> getTickets();

	CompletedOrder completeOrder();
	boolean isActive();
	void generateTickets(int numOfTickets, String segmentId, double sumOrderprice, String orderId);
	PaymentInfo getPaymentInfo();

}
