package com.group16b.ApplicationLayer.DTOs;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.group16b.DomainLayer.Event.Event;

public class EventDTO {
	private final int eventID;
	private boolean active = false;
	private String venueID;
	private String name;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private String artist;
	private String category;
	private final int productionCompanyID;
	private DiscountPolicyDTO discountPolicy;
	private PurchasePolicyDTO purchasePolicy;
	private double price;
	private double rating;

	public EventDTO(Event event) {
		eventID = event.getEventID();
		active = event.getEventStatus();
		venueID = event.getEventVenueID();
		name = event.getEventName();
		startTime = event.getEventStartTime();
		endTime = event.getEventEndTime();
		artist = event.getEventArtist();
		category = event.getEventCategory();
		productionCompanyID = event.getEventProductionCompanyID();
		discountPolicy = new DiscountPolicyDTO(event.getEventDiscountPolicy());
		purchasePolicy = new PurchasePolicyDTO(event.getEventPurchasePolicy());
		price = event.getEventPrice();
		rating = event.getEventRating();
	}

	public int getEventID() {
		return this.eventID;
	}

	public boolean getEventStatus() {
		return this.active;
	}

	public String getEventVenueID() {
		return this.venueID;
	}

	public String getEventName() {
		return this.name;
	}

	public LocalDateTime getEventStartTime() {
		return this.startTime;
	}

	public LocalDateTime getEventEndTime() {
		return this.endTime;
	}

	public String getEventArtist() {
		return this.artist;
	}

	public String getEventCategory() {
		return this.category;
	}

	public int getEventProductionCompanyID() {
		return this.productionCompanyID;
	}

	public DiscountPolicyDTO getEventDiscountPolicy() {
		return this.discountPolicy;
	}

	public PurchasePolicyDTO getEventPurchasePolicy() {
		return this.purchasePolicy;
	}

	public double getEventPrice() {
		return this.price;
	}

	public double getEventRating() {
		return this.rating;
	}
}
