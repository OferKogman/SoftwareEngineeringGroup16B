package com.group16b.DomainLayer.Order;

import java.util.List;


public class Order {
	private final String orderId;
	private OrderState state;
	private final String segmentId;
	private List<String> seats; // seat Ids
	private int numOfTickets;
	private final OrderType orderType;
	private static int idCounter = 0;

	


	protected Order(String segmentId, List<String> seats) {
		this.orderId = "order_" + ++idCounter;
		this.state = new ActiveOrder();
		this.seats = List.copyOf(seats);
		this.numOfTickets = seats.size();
		this.segmentId = segmentId;
		this.orderType = OrderType.SEAT;
	}
	protected Order(String segmentId, int amount) {
		this.orderId = "order_" + ++idCounter;
		this.state = new ActiveOrder();
		this.numOfTickets = amount;
		this.segmentId = segmentId;
		this.orderType = OrderType.FIELD;
	}
	protected String getOrderId() {
		return orderId;
	}

	protected OrderState getState() {
		return state;
	}

	protected String getSegmentId() {
		return segmentId;
	}

	protected List<String> getSeats() {
		if (orderType == OrderType.FIELD) {
			throw new IllegalStateException("This order is for field tickets, it does not have specific seats.");
		}
		return seats;
	}

	protected int getNumOfTickets() {
		return numOfTickets;
	}

	protected OrderType getOrderType() {
		return orderType;
	}

	protected void CompleteOrder() {
		this.state = state.completeOrder();
	}

	protected List<String> getTickets() {
		return state.getTickets();
	}


}
