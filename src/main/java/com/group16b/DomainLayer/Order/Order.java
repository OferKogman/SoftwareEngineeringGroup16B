package com.group16b.DomainLayer.Order;

import java.security.MessageDigest;
import java.util.List;

public class Order {
	private final String orderId;
	private OrderState state;
	private final String segmentId;
	private List<String> seats; // seat Ids
	private double pricesPerSeat;
	private int numOfTickets;
	private final OrderType orderType;
	private static int idCounter = 0;
	private  double sumOrderprice; // @TODO: calculate price based on the segment and number of tickets.
	private List<String> tickets; // List of tickets associated with this order
	private int eventId;
	private final String subjectID;


	public Order(String segmentId, List<String> seats, double pricesPerSeat, int eventId, String subjectID) {
		this.orderId = "order_" + ++idCounter;
		this.state = new ActiveOrder();
		this.seats = List.copyOf(seats);
		this.numOfTickets = seats.size();
		this.segmentId = segmentId;
		this.orderType = OrderType.SEAT;
		this.sumOrderprice = pricesPerSeat * seats.size();
		this.pricesPerSeat = pricesPerSeat;
		this.eventId = eventId;
		this.subjectID = subjectID;
	}
	public Order(String segmentId, int amount, double pricesPerSeat, int eventId, String subjectID) {
		this.orderId = "order_" + ++idCounter;
		this.state = new ActiveOrder();
		this.numOfTickets = amount;
		this.segmentId = segmentId;
		this.orderType = OrderType.FIELD;
		this.sumOrderprice = pricesPerSeat * amount;
		this.pricesPerSeat = pricesPerSeat;
		this.eventId = eventId;
		this.subjectID = subjectID;
		}

	public String getOrderId() {
		return orderId;
	}
	public int getEventId() {
		return eventId;
	}
	public String getSubjectId() {
		return this.subjectID;
	}

	public OrderState getState() {
		return state;
	}

	public String getSegmentId() {
		return segmentId;
	}

	public List<String> getSeats() {
		if (orderType == OrderType.FIELD) {
			throw new IllegalStateException("This order is for field tickets, it does not have specific seats.");
		}
		return seats;
	}

	public double getPricesPerSeat() {
		return pricesPerSeat;
	}

	public int getNumOfTickets() {
		return numOfTickets;
	}

	public OrderType getOrderType() {
		return orderType;
	}

	public boolean CompleteOrder() {
		this.state = state.completeOrder();
		return true;
	}

	public double getSumOrderprice() {
		return sumOrderprice;
	}

	
	public boolean isActive() {
		return state.isActive();
	}
	public boolean isBelongsToSubject(String subjectID) {
		return this.subjectID.equals(subjectID);
	}	


	public void updateSeats(List<String> newSeatIds) {
		if (orderType == OrderType.FIELD) {
			throw new IllegalStateException("This order is for field tickets, it does not have specific seats.");
		}
		if (newSeatIds == null || newSeatIds.isEmpty()) {
			throw new IllegalArgumentException("New seat IDs list cannot be null or empty");
		}
		this.seats = List.copyOf(newSeatIds);
		this.numOfTickets = newSeatIds.size();
	}
	public void updateNumOfTickets(int newNumOfTickets) {
		if (orderType == OrderType.SEAT) {
			throw new IllegalStateException("This order is for seat tickets, it must have specific seats.");
		}
		if (newNumOfTickets <= 0) {
			throw new IllegalArgumentException("New number of tickets must be greater than zero");
		}
		this.numOfTickets = newNumOfTickets;
	}

	

	public String encodeStocken(String sTocken) {
		try{
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(sTocken.getBytes());
			String stringHash = new String(messageDigest.digest());
			return stringHash;
		} catch (Exception e) {
			throw new RuntimeException("Error hashing password: " + e.getMessage());
		}
	}

	
	

}
