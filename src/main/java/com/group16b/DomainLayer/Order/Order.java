package com.group16b.DomainLayer.Order;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;


public class Order {
	private final String orderId;
	private OrderState state;
	private final String segmentId;
	private List<String> seats; // seat Ids
	private List<Double> pricesPerSeat;
	private int numOfTickets;
	private final OrderType orderType;
	private static int idCounter = 0;
	private final String incodedSTocken;
	private  double sumOrderprice; // @TODO: calculate price based on the segment and number of tickets.
	private List<Ticket> tickets; // List of tickets associated with this order
	


	public Order(String segmentId, List<String> seats, String STocken, double pricesPerSeat, int eventId, int userId) {
		this.orderId = "order_" + ++idCounter;
		this.state = new ActiveOrder();
		this.seats = List.copyOf(seats);
		this.numOfTickets = seats.size();
		this.segmentId = segmentId;
		this.incodedSTocken = encodeStocken(STocken);
		this.orderType = OrderType.SEAT;
		this.sumOrderprice = pricesPerSeat * seats.size();
		
	}
	public Order(String segmentId, int amount, String STocken, double pricesPerTicket, int eventId, int userId) {
		this.orderId = "order_" + ++idCounter;
		this.state = new ActiveOrder();
		this.numOfTickets = amount;
		this.segmentId = segmentId;
		this.incodedSTocken = encodeStocken(STocken);
		this.orderType = OrderType.FIELD;
		this.sumOrderprice = pricesPerTicket * amount;

		
		}

	public String getOrderId() {
		return orderId;
	}

	public OrderState getState() {
		return state;
	}

	public void generateTickets(int eventId, int userId) {
		state.generateTickets(numOfTickets, segmentId, sumOrderprice, orderId);
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

	public List<Double> getPricesPerSeat() {
		if (orderType == OrderType.FIELD) {
			List<Double> fieldPrices = new ArrayList<>();
			for (int i = 0; i < numOfTickets; i++) {
				fieldPrices.add(sumOrderprice / numOfTickets); // Distribute total price equally among field tickets
			}
			return fieldPrices;
		}
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

	public List<Ticket> getTickets() {
		return state.getTickets();
	}
	public double getSumOrderprice() {
		return sumOrderprice;
	}

	
	public boolean isActive() {
		return state.isActive();
	}
	public boolean isBelongsToUser(String userId) {
		String encodedUserId = encodeStocken(userId);
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
	public PaymentInfo getPaymentInfo() {
		return state.getPaymentInfo();
	}

}
