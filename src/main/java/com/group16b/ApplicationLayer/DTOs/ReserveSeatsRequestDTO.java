package com.group16b.ApplicationLayer.DTOs;

import java.util.List;

public class ReserveSeatsRequestDTO {
    String segmentId;
    List<String> seatIds;
    String venueId;

    public ReserveSeatsRequestDTO(String segmentId, List<String> seatIds, String venueId) {
        this.segmentId = segmentId;
        this.seatIds = seatIds;
        this.venueId = venueId;
    }
    public ReserveSeatsRequestDTO() {
    }
    public String getSegmentId() {
        return segmentId;
    }
    public List<String> getSeatIds() {
        return seatIds;
    }
    public String getVenueId() {
        return venueId;
    }

    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }
    public void setSeatIds(List<String> seatIds) {
        this.seatIds = seatIds;
    }
    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }
    
}
