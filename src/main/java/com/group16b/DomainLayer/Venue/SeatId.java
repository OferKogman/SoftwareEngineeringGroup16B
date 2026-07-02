package com.group16b.DomainLayer.Venue;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class SeatId implements Serializable {

    @Column(name = "venue_id")
    private String venueId;

    @Column(name = "segment_id")
    private String segmentId;

    @Column(name = "seat_id")
    private String seatId;

    public SeatId() {}

    public SeatId(String venueId, String segmentId, String seatId) {
        this.venueId = venueId;
        this.segmentId = segmentId;
        this.seatId = seatId;
    }

    public String getVenueId() {
        return venueId;
    }

    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }

    public String getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeatId other)) return false;
        return Objects.equals(venueId, other.venueId)
                && Objects.equals(segmentId, other.segmentId)
                && Objects.equals(seatId, other.seatId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(venueId, segmentId, seatId);
    }
}