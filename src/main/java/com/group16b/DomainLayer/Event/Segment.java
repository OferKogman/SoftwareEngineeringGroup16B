package com.group16b.DomainLayer.Event;

import java.lang.reflect.Type;

abstract class Segment {
    private final String segmentID;

    Segment(String segmentID) {
        this.segmentID = segmentID;
    }

    String getSegmentID() {
        return segmentID;
    }

    abstract String getSegmentType();
}
