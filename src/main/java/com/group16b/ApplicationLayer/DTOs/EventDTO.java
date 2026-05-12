package com.group16b.ApplicationLayer.DTOs;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
	private final Set<DiscountPolicyDTO> discountPolicy;
	private final Set<PurchasePolicyDTO> purchasePolicy;
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
		discountPolicy = new HashSet<>();
		for (var policy : event.getEventDiscountPolicy()) {
			discountPolicy.add(new DiscountPolicyDTO(policy));
		}
		purchasePolicy = new HashSet<>();
		for (var policy : event.getEventPurchasePolicy()) {
			purchasePolicy.add(new PurchasePolicyDTO(policy));
		}
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

	public Set<DiscountPolicyDTO> getEventDiscountPolicy() {
		return this.discountPolicy;
	}

	public Set<PurchasePolicyDTO> getEventPurchasePolicy() {
		return this.purchasePolicy;
	}

	public double getEventPrice() {
		return this.price;
	}

	public double getEventRating() {
		return this.rating;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof EventDTO)) {
			return false;
		}
		EventDTO event = (EventDTO) o;
		return eventID == event.eventID;
	}
}
