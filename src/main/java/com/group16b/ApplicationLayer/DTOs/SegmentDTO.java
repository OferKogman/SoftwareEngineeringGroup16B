package com.group16b.ApplicationLayer.DTOs;

import java.util.HashMap;
import java.util.Map;

import com.group16b.DomainLayer.Venue.Segment;

public abstract class SegmentDTO {
    private final String segmentID;
    protected Map<Integer, Double> eventPrices = new HashMap<>();

    public SegmentDTO(Segment segment) {
        this.segmentID = segment.getSegmentID();
        this.eventPrices = new HashMap<>(segment.getEventPrices());
    }

    public String getSegmentID() {
        return this.segmentID;
    }

    public Map<Integer, Double> getEventPrices() {
        return this.eventPrices;
    }
}