package com.group16b.DomainLayer.Order;

import java.util.List;

import com.group16b.ApplicationLayer.Records.PaymentInfo;

interface OrderState {

	List<String> getTickets();
	void setTickets(List<String> tickets);
	CompletedOrder completeOrder();
	boolean isActive();

}
