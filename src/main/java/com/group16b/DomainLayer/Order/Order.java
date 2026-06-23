package com.group16b.DomainLayer.Order;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "orders")
public class Order {
 
    @Id
    private final String orderId;
 
    @Convert(converter = OrderStateConverter.class)
    @Column(nullable = false, columnDefinition = "TEXT")
    private OrderState state;
 
    @Column(nullable = false)
    private final String segmentId;
 
    @ElementCollection
    @CollectionTable(name = "order_seats", joinColumns = @JoinColumn(name = "order_id"))
    @Column(name = "seat_id")
    private List<String> seats;
 
    @Column(nullable = false)
    private int numOfTickets;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private final OrderType orderType;
 
    private static int idCounter = 0;
 
    @Column(nullable = false)
    private double totalOrderprice;
 
    @Column(nullable = false)
    private final int eventId;
 
    @Column(nullable = false)
    private final String subjectID;
 
    @Version
    private long version;
 
    @Column(name = "transaction_id")
    private Integer transactioId = null;
 
    @Column(name = "external_ticket")
    private String externalTicket = null;


	public Order(String segmentId, List<String> seats, double totalPrice, int eventId, String subjectID) {
		this.orderId = "order_" + ++idCounter;
		this.state = new ActiveOrder();
		this.seats = List.copyOf(seats);
		this.numOfTickets = seats.size();
		this.segmentId = segmentId;
		this.orderType = OrderType.SEAT;
		this.totalOrderprice = totalPrice;
		this.eventId = eventId;
		this.subjectID = subjectID;
		this.version = 0;
	}
	public Order(String segmentId, int amount, double totalPrice, int eventId, String subjectID) {
		this.orderId = "order_" + ++idCounter;
		this.state = new ActiveOrder();
		this.numOfTickets = amount;
		this.segmentId = segmentId;
		this.orderType = OrderType.FIELD;
		this.totalOrderprice = totalPrice;
		this.eventId = eventId;
		this.subjectID = subjectID;
		this.version = 0;
		this.seats = new ArrayList<>();
	}

	public Order(Order other) { // deep copy constructor
		this.orderId = other.orderId;
		this.state = other.state.copy();
		this.segmentId = other.segmentId;
		this.seats = List.copyOf(other.seats);
		this.numOfTickets = other.numOfTickets;
		this.orderType = other.orderType;
		this.totalOrderprice = other.totalOrderprice;
		this.eventId = other.eventId;
		this.subjectID = other.subjectID;
		this.version = other.version;
		this.transactioId=other.transactioId;
		this.externalTicket=other.externalTicket;
		
	}

	public Order(){ //default constructor for JPA
        this.orderId = null;
        this.segmentId = null;
        this.orderType = null;
        this.eventId = 0;
        this.subjectID = null;
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
	public long getOrderStartTime() {
		if (state instanceof ActiveOrder activeOrder) {
			return activeOrder.getCreationTime();
		} else {
			throw new IllegalStateException("Cannot get order start time for a completed or canceled order");
		}
	}

	public List<String> getSeats() {
		if (orderType == OrderType.FIELD) {
			throw new IllegalStateException("This order is for field tickets, it does not have specific seats.");
		}
		return seats;
	}


	public int getNumOfTickets() {
		return numOfTickets;
	}

	public OrderType getOrderType() {
		return orderType;
	}

	public Integer getTransactionId()
	{
		return transactioId;
	}

	public String getExternalTicket()
	{
		return externalTicket;
	}

	public void setTransactionId(int transactionId)
	{
		this.transactioId=transactionId;
	}

	public void setExternalTicket(String ticket)
	{
		this.externalTicket=ticket;
	}

	public void verifyTypeSeats(){
		if (orderType != OrderType.SEAT) {
			throw new IllegalStateException("This order is for field tickets, it does not have specific seats.");
		}
	}
	public void verifyTypeField(){
		if (orderType != OrderType.FIELD) {
			throw new IllegalStateException("This order is for seat tickets, it must have specific seats.");
		}
	}

	public boolean CompleteOrder() {
		this.state = state.completeOrder();
		return true;
	}
	public void CancelOrder() {
		this.state = new CanceledOrder();
	}

	public double getTotalOrderprice() {
		return totalOrderprice;
	}

	
	public boolean isActive() {
		return state.isActive();
	}
	public boolean isCompleted() {
		return state.isCompleted();
	}

	public void validiteOrderIsActive() {
		if (!this.isActive()) {
			throw new IllegalStateException("Order " + this.orderId + " is not active");
		}
	}


	public boolean isBelongsToSubject(String subjectID) {
		return this.subjectID.equals(subjectID);
	}

	public void verifyBelongsToSubject(String subjectID) {
		if (!this.subjectID.equals(subjectID)){
			throw new IllegalArgumentException("Order " + this.orderId + " does not belong to subject " + subjectID);
		}
	}	


	public void updateSeats(List<String> newSeatIds, double newTotalPrice) {
		if (orderType == OrderType.FIELD) {
			throw new IllegalStateException("This order is for field tickets, it does not have specific seats.");
		}
		if (newSeatIds == null || newSeatIds.isEmpty()) {
			throw new IllegalArgumentException("New seat IDs list cannot be null or empty");
		}
		this.seats = List.copyOf(newSeatIds);
		this.numOfTickets = newSeatIds.size();
		this.totalOrderprice = newTotalPrice;
	}
	public void updateNumOfTickets(int newNumOfTickets, double newTotalPrice) {
		if (orderType == OrderType.SEAT) {
			throw new IllegalStateException("This order is for seat tickets, it must have specific seats.");
		}
		if (newNumOfTickets <= 0) {
			throw new IllegalArgumentException("New number of tickets must be greater than zero");
		}
		this.numOfTickets = newNumOfTickets;
		this.totalOrderprice = newTotalPrice;
	}

	public long getVersion()
    {
        return version;
    }
    public void setVersion(long version)
    {
        this.version=version;
    }

	
	

}
