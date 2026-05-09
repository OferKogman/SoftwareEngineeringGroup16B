package com.group16b.ApplicationLayer.DTOs;

import com.group16b.DomainLayer.Order.Ticket;

public class TicketDTO {
    private final String ticketId;
    private final int eventId;
    private final int userId;
    private final String segmentName; // Optional, can be null for field ticket
    private final String seatNumber; // Optional, can be null for field tickets
    private final double price;
    public TicketDTO(Ticket ticket) {
        this.ticketId = ticket.getTicketId();
        this.eventId = ticket.getEventId();
        this.userId = ticket.getUserId();
        this.segmentName = ticket.getSegmentName();
        this.seatNumber = ticket.getSeatNumber();
        this.price = ticket.getPrice();
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
