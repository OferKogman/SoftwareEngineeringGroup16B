package com.group16b.ApplicationLayer.Records;

import java.time.LocalDateTime;

import com.group16b.DomainLayer.Policies.DiscountPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy;

public record EventRecord(String venueID,
		String name,
		LocalDateTime startTime,
		LocalDateTime endTime,
		String artist,
		String category,
		int pcID,
		DiscountPolicy discountPolicy,
		PurchasePolicy purchasePolicy,
		double price,
		double rating) {
}
