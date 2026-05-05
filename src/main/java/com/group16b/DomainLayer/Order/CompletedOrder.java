package com.group16b.DomainLayer.Order;

import java.util.List;

class CompletedOrder implements OrderState {
	private final List<String> tickets;

	CompletedOrder(List<String> tickets) {
		this.tickets = tickets;
	}

	@Override
	public List<String> getTickets() {
		return tickets; // Return all tickets
	}

	@Override
	public CompletedOrder completeOrder() {
		throw new IllegalStateException("Order is already completed.");
	}
}
