package com.group16b.ApplicationLayer.DTOs;

import com.group16b.DomainLayer.Venue.Segment;

public abstract class SegmentDTO {
    private final String segmentID;     

    public SegmentDTO(Segment segment) {
        this.segmentID = segment.getSegmentID();
    }


	public abstract String getSegmentType();
}