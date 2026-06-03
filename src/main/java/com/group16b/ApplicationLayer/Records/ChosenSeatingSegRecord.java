package com.group16b.ApplicationLayer.Records;

import java.util.List;

public record ChosenSeatingSegRecord(String segmentID, List<SeatRecord> seats, GridRectangleRecord area) {};
