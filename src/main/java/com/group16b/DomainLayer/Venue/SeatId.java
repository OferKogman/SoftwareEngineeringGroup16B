package com.group16b.DomainLayer.Venue;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class SeatId implements Serializable {

    @Column(name = "seat_id")
    private String seatId;

    @Column(name = "segment_id")
    private String segmentId;

    public SeatId() {}

    public SeatId(String seatId, String segmentId) {
        this.seatId = seatId;
        this.segmentId = segmentId;
    }

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    public String getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeatId other)) return false;
        return Objects.equals(seatId, other.seatId)
                && Objects.equals(segmentId, other.segmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seatId, segmentId);
    }
}