package com.group16b.DomainLayer.Order;

import java.util.List;

class ActiveOrder implements OrderState {
	private final long creationTime;
    private static final long ORDER_TIMEOUT = 10 * 60 * 1000; // 10 minutes in milliseconds

	protected ActiveOrder() {
		this.creationTime = System.currentTimeMillis();
	}

	@Override
	public List<String> getTickets() {

		throw new IllegalStateException("Cannot get tickets from an active order");
		// return null; // can only get tickets from completed order
	}

	@Override
	public CompletedOrder completeOrder() {
		validateTime();
		List<String> tickets = List.of(); // Generate tickets based on seats
		return new CompletedOrder(tickets);
	}

	private void validateTime() {
        long currentTime = System.currentTimeMillis();
		if ((currentTime - creationTime) > ORDER_TIMEOUT){
			throw new IllegalStateException("This Order is Expired.");
		}
	}

}
