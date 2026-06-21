package com.group16b.ApplicationLayer.Records;

import java.util.List;

public record EventSeatSegmentConfigUpdateRecord(List<String> newSeatsIDs, double newPrice) {
}
