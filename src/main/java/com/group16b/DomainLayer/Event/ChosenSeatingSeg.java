package com.group16b.DomainLayer.Event;

import java.lang.reflect.Type;
import java.util.*;

class ChosenSeatingSeg extends Segment {
    protected final Map<String, Seat> seats;

    ChosenSeatingSeg(String segmentID, Map<String, Seat> seats) {
        super(segmentID);
        this.seats = seats;
    }

    @Override
    String getSegmentType() {
        return "S";
    }
}
