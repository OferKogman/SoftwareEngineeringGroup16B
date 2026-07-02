package com.group16b.DomainLayer.Venue;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class SegmentId implements Serializable {

    @Column(name = "venue_id")
    private String venueId;

    @Column(name = "segment_id")
    private String segmentId;

    public SegmentId() {}

    public SegmentId(String venueId, String segmentId) {
        this.venueId = venueId;
        this.segmentId = segmentId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SegmentId other)) return false;
        return Objects.equals(venueId, other.venueId)
                && Objects.equals(segmentId, other.segmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(venueId, segmentId);
    }
}