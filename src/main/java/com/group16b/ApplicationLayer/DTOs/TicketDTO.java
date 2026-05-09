package com.group16b.ApplicationLayer.DTOs;


public class TicketDTO {
    private final String ticketId;
    private final int eventId;
    private final int userId;
    private final String segmentName; // Optional, can be null for field ticket
    private final String seatNumber; // Optional, can be null for field tickets
    private final double price;
    public TicketDTO(int eventId, int userId, String segmentId, String seatId, double price) {
        this.ticketId = "ticket_" + eventId + "_" + segmentId + "_" + seatId; // Generate a unique ticket ID based on event, segment, and seat
        this.eventId = eventId;
        this.userId = userId;
        this.segmentName = segmentId;
        this.seatNumber = seatId;
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
