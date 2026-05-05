package com.group16b.DomainLayer.Order;

import java.util.List;

interface OrderState {

	List<String> getTickets();

	CompletedOrder completeOrder();
}
