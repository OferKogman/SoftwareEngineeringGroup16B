package com.group16b.ApplicationLayer.Records;

import java.time.LocalDateTime;

public record EventRecord(String venueID,
		String name,
		LocalDateTime startTime,
		LocalDateTime endTime,
		String artist,
		String category,
		int pcID,
		double price,
		double rating) {
}
