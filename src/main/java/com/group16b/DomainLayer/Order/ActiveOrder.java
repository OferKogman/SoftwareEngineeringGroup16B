package com.group16b.DomainLayer.Order;

import java.util.List;

class ActiveOrder implements OrderState {
	private final long creationTime;
    private static final long ORDER_TIMEOUT = 10 * 60 * 1000; // 10 minutes in milliseconds

	public ActiveOrder() {
		this.creationTime = System.currentTimeMillis();
	}

	@Override
	public boolean isActive() {
		validateTime();
		return true;
	}

	@Override
	public List<String> getTickets() {
		throw new IllegalStateException("Cannot get tickets from an active order");
	}

	@Override
	public void setTickets(List<String> tickets) {
		throw new IllegalStateException("Cannot set tickets for an active order");
	}

	@Override
	public CompletedOrder completeOrder() {
		validateTime();
		return new CompletedOrder();
	}

	private void validateTime() {
        long currentTime = System.currentTimeMillis();
		if ((currentTime - creationTime) > ORDER_TIMEOUT){
			throw new IllegalStateException("This Order is Expired.");
		}
	}


}
