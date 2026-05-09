package com.group16b.DomainLayer.Venue;

import java.util.List;

class ReservationRequest {
    private final int eventID;
    private final List<String> seatIds;
    private final Integer quantity;
    private final String segmentId;

    private ReservationRequest(int eventID, List<String> seatIds, Integer quantity, String segmentId) {
        this.eventID = eventID;
        this.seatIds = seatIds;
        this.quantity = quantity;
        this.segmentId = segmentId;
    }

    static ReservationRequest forSeats(int eventID, List<String> seatIds, String segmentId) {
        return new ReservationRequest(eventID, seatIds, null, segmentId);
    }

    static ReservationRequest forField(int eventID, int quantity, String segmentId) {
        return new ReservationRequest(eventID, null, quantity, segmentId);
    }

    int getEventID() {
        return eventID;
    }

    List<String> getSeatIds() {
        return seatIds;
    }

    int getQuantity() {
        return quantity;
    }

    String getSegmentId() {
        return segmentId;
    }
}
