package com.group16b.DomainLayer.Order;

public enum OrderType {
	SEAT("Seat"), FIELD("Field");

    public static OrderType getSEAT() {
        return SEAT;
    }

    public static OrderType getFIELD() {
        return FIELD;
    }

	private final String displayName;

	OrderType(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}