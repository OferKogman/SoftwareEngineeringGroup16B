package com.group16b.ApplicationLayer.DTOs;

import java.util.List;

import com.group16b.DomainLayer.Order.Order;

public class ActiveOrderDTO {

    private final String orderId;
    private final String segmentId;
    private final int numOfTickets;
    private final String orderType;
    private final double tocalOrderPrice;
    private final int eventId;
    private final String subjectID;
    private final long orderStartTime;
    private final List<String> seats;

    public ActiveOrderDTO(Order order){
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        if (!order.isActive()) {
            throw new IllegalArgumentException("Cannot create ActiveOrderDTO from a completed order");
        }
        this.orderId = order.getOrderId();
        this.segmentId = order.getSegmentId();
        this.numOfTickets = order.getNumOfTickets();
        this.orderType = order.getOrderType().toString();
        this.tocalOrderPrice = order.getTotalOrderprice();
        this.eventId = order.getEventId();
        this.subjectID = order.getSubjectId();
        this.orderStartTime = order.getOrderStartTime();
        this.seats = order.getOrderType().toString().equals("SEAT") ? order.getSeats() : List.of();
    }

    public String getOrderId() {
        return orderId;
    }
    public String getSegmentId() {
        return segmentId;
    }

    public int getNumOfTickets() {
        return numOfTickets;
    }
    public String getOrderType() {
        return orderType;
    }
    public double getTocalOrderPrice() {
        return tocalOrderPrice;
    }
    public int getEventId() {
        return eventId;
    }
    public String getSubjectId() {
        return subjectID;
    }
    public long getOrderStartTime() {
        return orderStartTime;
    }
    public List<String> getSeats() {
        return seats;
    }
    
}
