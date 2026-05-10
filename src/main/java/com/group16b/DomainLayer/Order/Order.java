package com.group16b.DomainLayer.Order;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import com.group16b.ApplicationLayer.PaymentInfo;


public class Order {
	private final String orderId;
	private OrderState state;
	private final String segmentId;
	private List<String> seats; // seat Ids
	private double pricesPerSeat;
	private int numOfTickets;
	private final OrderType orderType;
	private static int idCounter = 0;
	private final String incodedSTocken;
	private  double sumOrderprice; // @TODO: calculate price based on the segment and number of tickets.
	private List<String> tickets; // List of tickets associated with this order
	private int eventId;
	


	public Order(String segmentId, List<String> seats, String STocken, double pricesPerSeat, int eventId, int userId) {
		this.orderId = "order_" + ++idCounter;
		this.state = new ActiveOrder();
		this.seats = List.copyOf(seats);
		this.numOfTickets = seats.size();
		this.segmentId = segmentId;
		this.incodedSTocken = encodeStocken(STocken);
		this.orderType = OrderType.SEAT;
		this.sumOrderprice = pricesPerSeat * seats.size();
		this.pricesPerSeat = pricesPerSeat;
		this.eventId = eventId;
		
	}
	public Order(String segmentId, int amount, String STocken, double pricesPerSeat, int eventId, int userId) {
		this.orderId = "order_" + ++idCounter;
		this.state = new ActiveOrder();
		this.numOfTickets = amount;
		this.segmentId = segmentId;
		this.incodedSTocken = encodeStocken(STocken);
		this.orderType = OrderType.FIELD;
		this.sumOrderprice = pricesPerSeat * amount;
		this.pricesPerSeat = pricesPerSeat;
		this.eventId = eventId;

		
		}

	public String getOrderId() {
		return orderId;
	}
	public int getEventId() {
		return eventId;
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
	public boolean isBelongsToUser(String sTocken) {
		String encodedUserId = encodeStocken(String.valueOf(sTocken));
		return this.incodedSTocken.equals(encodedUserId);
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
