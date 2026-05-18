package com.group16b.ApplicationLayer.Records;

import java.util.List;

public record VenueRecord(String name, String location, List<FieldSegRecord> fieldSeg, List<ChosenSeatingSegRecord> seatSeg) {}