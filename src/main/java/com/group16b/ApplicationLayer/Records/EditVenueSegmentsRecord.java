package com.group16b.ApplicationLayer.Records;

import java.util.List;
import java.util.Map;

public record EditVenueSegmentsRecord(
    Map<String, Integer> fieldSegmentsToEdit,
    Map<String, List<String>> seatsSegmentsToEdit,
    List<FieldSegRecord> newFieldSegments,
    List<ChosenSeatingSegRecord> newSeatSegments
) {}