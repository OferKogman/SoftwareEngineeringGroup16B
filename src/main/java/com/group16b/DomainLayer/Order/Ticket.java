package com.group16b.DomainLayer.Order;

public class Ticket {
    private final String ticketId;
    private final int eventId;
    private final int userId;
    private final String segmentName; // Optional, can be null for field ticket
    private final String seatNumber; // Optional, can be null for field tickets
    private final double price;

    public Ticket(String ticketId, int eventId, int userId, String segmentName, String seatNumber, double price) {
        this.ticketId = ticketId;
        this.eventId = eventId;
        this.userId = userId;
        this.segmentName = segmentName;
        this.seatNumber = seatNumber;
        this.price = price;
    }

    public String getTicketId() {
        return ticketId;
    }

    public int getEventId() {
        return eventId;
    }

    public int getUserId() {
        return userId;
    }

    public String getSegmentName() {
        return segmentName;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public double getPrice() {
        return price;
    }
}
