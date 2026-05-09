package com.group16b.DomainLayer.Order;

import java.util.ArrayList;
import java.util.List;

class CompletedOrder implements OrderState {
	private List<Ticket> tickets;
	private PaymentInfo paymentInfo;

	CompletedOrder(PaymentInfo paymentInfo) {
		this.paymentInfo = paymentInfo;
	}

	@Override
	public void generateTickets(int numOfTickets, String segmentId, double sumOrderprice, String orderId) {
		List<Ticket> generatedTickets = new ArrayList<>();
		for (int i = 0; i < numOfTickets; i++) {
			String ticketId = "ticket_" + orderId + "_" + (i + 1);
			Ticket ticket = new Ticket(ticketId, 0, 0, segmentId, null, sumOrderprice / numOfTickets); // @TODO: set eventId and userId when creating the ticket
			generatedTickets.add(ticket);
		}
		this.tickets = generatedTickets;
	}

	@Override
	public List<Ticket> getTickets() {
		if (tickets == null) {
			throw new IllegalStateException("Tickets have not been generated yet for this order.");
		}
		return tickets; 
	}

	@Override
	public boolean isActive() {
		return false;
	}

	@Override
	public CompletedOrder completeOrder() {
		throw new IllegalStateException("Order is already completed.");
	}

	@Override
	public PaymentInfo getPaymentInfo() {
		PaymentInfo p = this.paymentInfo;
		this.paymentInfo = null; // @TODO Ofer what do you thing? Clear payment info after retrieval for security reasons
		return p;
	}
	
}
