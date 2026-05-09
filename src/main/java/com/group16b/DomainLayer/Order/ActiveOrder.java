package com.group16b.DomainLayer.Order;

import java.util.List;

class ActiveOrder implements OrderState {
	private final long creationTime;
    private static final long ORDER_TIMEOUT = 10 * 60 * 1000; // 10 minutes in milliseconds

	protected ActiveOrder() {
		this.creationTime = System.currentTimeMillis();
	}

	@Override
	public boolean isActive() {
		validateTime();
		return true;
	}

	@Override
	public List<Ticket> getTickets() {

		throw new IllegalStateException("Cannot get tickets from an active order");
	}

	@Override
	public CompletedOrder completeOrder() {
		PaymentInfo paymentInfo = null; // @TODO:  = getPaymentInfoFromUser();
		if (!paymentInfo.isvalid())
			throw new IllegalStateException("Invalid payment information.");
		validateTime();
		return new CompletedOrder(paymentInfo);
	}

	private void validateTime() {
        long currentTime = System.currentTimeMillis();
		if ((currentTime - creationTime) > ORDER_TIMEOUT){
			throw new IllegalStateException("This Order is Expired.");
		}
	}
	@Override
	public void generateTickets(int numOfTickets, String segmentId, double sumOrderprice, String orderId) {
		throw new IllegalStateException("Cannot generate tickets for an active order");
	}
	@Override
	public PaymentInfo getPaymentInfo() {
		throw new IllegalStateException("Cannot get payment info from an active order");
	}

}
