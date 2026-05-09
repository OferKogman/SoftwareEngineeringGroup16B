package com.group16b.DomainLayer.Order;

public enum OrderType {
	SEAT("Seat"), FIELD("Field");

	private final String displayName;

	OrderType(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}