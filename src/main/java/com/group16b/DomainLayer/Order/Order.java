package com.group16b.DomainLayer.Order;

import java.util.Date;
import java.util.List;

class Order {
	private final String orderId;
	private final Date orderDate;
	private OrderState state;
	private final String segmentId;
	private List<String> seats; // seat Ids
	private int numOfTickets;
	private final OrderType orderType;

	protected Order(String orderId, String segmentId, List<String> seats) {
		this.orderId = orderId;
		this.state = new ActiveOrder();
		this.seats = List.copyOf(seats);
		this.numOfTickets = seats.size();
		this.segmentId = segmentId;
		this.orderType = OrderType.SEAT;
		this.orderDate = new Date(System.currentTimeMillis()); // get time
	}

	protected Order(String orderId, String segmentId, int amount) {
		this.orderId = orderId;
		this.state = new ActiveOrder();
		this.numOfTickets = amount;
		this.segmentId = segmentId;
		this.orderType = OrderType.FIELD;
		this.orderDate = new Date(System.currentTimeMillis()); // get time
	}

	protected String getOrderId() {
		return orderId;
	}

	protected Date getOrderDate() {
		return orderDate;
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
