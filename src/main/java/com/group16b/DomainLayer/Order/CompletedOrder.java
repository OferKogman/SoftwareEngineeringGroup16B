package com.group16b.DomainLayer.Order;

import java.util.List;


class CompletedOrder implements OrderState {
	private List<String> tickets;

	public CompletedOrder() {}


	

	@Override
	public boolean isActive() {
		return false;
	}

	@Override
	public CompletedOrder completeOrder() {
		throw new IllegalStateException("Order is already completed.");
	}

	@Override
	public void setTickets(List<String> tickets) {
		this.tickets = List.copyOf(tickets);
	}

	@Override
	public List<String> getTickets() {
		if (tickets == null) {
			throw new IllegalStateException("Tickets have not been set for this completed order.");
		}
		return tickets;
	}
	
}
