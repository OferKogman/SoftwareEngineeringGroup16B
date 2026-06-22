package com.group16b.ApplicationLayer.DTOs;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.AndDTO;
import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.MaxAgeDTO;
import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.MaxTicketsDTO;
import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.MinAgeDTO;
import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.MinTicketsDTO;
import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.OrDTO;
import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.PurchasePolicyDTO;
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
	private final PurchasePolicyDTO purchasePolicy;
	private double price;
	private double rating;

	private LotteryDTO lotteryDTO;

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
		purchasePolicy = new AndDTO(new OrDTO(new AndDTO(new MinAgeDTO(
				55), new MaxTicketsDTO(5)), new MaxAgeDTO(18)),
				new OrDTO(new MinTicketsDTO(10), new MaxTicketsDTO(2)));
		price = event.getEventPrice();
		rating = event.getEventRating();

		lotteryDTO = event.hasLotteryPolicy() ? new LotteryDTO(event.getLotteryPolicy()) : null;
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

	public PurchasePolicyDTO getEventPurchasePolicy() {
		return this.purchasePolicy;
	}

	public double getEventPrice() {
		return this.price;
	}

	public double getEventRating() {
		return this.rating;
	}

	public LotteryDTO getLotteryDTO() {
		return this.lotteryDTO;
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
