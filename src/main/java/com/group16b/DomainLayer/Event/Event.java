package com.group16b.DomainLayer.Event;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.DomainLayer.Policies.DiscountPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.LotteryPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;
import com.group16b.DomainLayer.ProductionCompanyPolicy.ProductionCompanyPolicy;

public class Event {
	private static int IDCounter = 1;

	private final int eventID;
	private AtomicBoolean active = new AtomicBoolean(false);
	private String venueID;
	private String name;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private String artist;
	private String category;
	private final int productionCompanyID;
	private Set<DiscountPolicy> discountPolicy;
	private Set<PurchasePolicy> purchasePolicy;
	private double price;
	private double rating;

	public Event(EventRecord eventRecord) {
		this.eventID = IDCounter++;
		this.venueID = eventRecord.venueID();
		validateName(eventRecord.name());
		this.name = eventRecord.name();
		validateDates(eventRecord.startTime(), eventRecord.endTime());
		this.startTime = eventRecord.startTime();
		this.endTime = eventRecord.endTime();
		validateArtist(eventRecord.artist());
		this.artist = eventRecord.artist();
		validateCategory(eventRecord.category());
		this.category = eventRecord.category();
		this.productionCompanyID = eventRecord.pcID();
		this.discountPolicy = Collections.synchronizedSet(new HashSet<>());
		this.purchasePolicy = Collections.synchronizedSet(new HashSet<>());
		validatePrice(eventRecord.price());
		this.price = eventRecord.price();
		validateRating(eventRecord.rating());
		this.rating = eventRecord.rating();
	}

	public int getEventID() {
		return eventID;
	}

	public boolean getEventStatus() {
		return active.get();
	}

	public void activateEvent() {
		if (active.getAndSet(true)) {
			throw new IllegalStateException("Event is already active.");
		}
	}

	public void deactivateEvent() {
		if (!active.getAndSet(false)) {
			throw new IllegalStateException("Event is already inactive.");
		}
	}

	public String getEventVenueID() {
		return venueID;
	}

	public void setEventString(String venueID) {
		this.venueID = venueID;
		// initialize stock and handle old sales
	}

	public String getEventName() {
		return name;
	}

	public void setEventName(String name) {
		this.name = name;
	}

	public LocalDateTime getEventStartTime() {
		return startTime;
	}

	public LocalDateTime getEventEndTime() {
		return endTime;
	}

	public void setEventNewTime(LocalDateTime startTime, LocalDateTime endTime) {
		validateDates(startTime, endTime);
	}

	public String getEventArtist() {
		return artist;
	}

	public void setEventArtist(String artist) {
		this.artist = artist;
	}

	public String getEventCategory() {
		return category;
	}

	public void setEventCategory(String category) {
		this.category = category;
	}

	public int getEventProductionCompanyID() {
		return productionCompanyID;
	}

	public Set<DiscountPolicy> getEventDiscountPolicy() {
		synchronized (discountPolicy) {
        return new HashSet<>(discountPolicy);
    }
	}

	public void addEventDiscountPolicy(DiscountPolicy dp) {
		discountPolicy.add(dp);
	}

	public void removeEventDiscountPolicy(DiscountPolicy dp) {
		discountPolicy.remove(dp);
	}

	public Set<PurchasePolicy> getEventPurchasePolicy() {
		synchronized (purchasePolicy) {
        return new HashSet<>(purchasePolicy);
    }
	}

	public void addEventPurchasePolicy(PurchasePolicy pp) {
		purchasePolicy.add(pp);
	}

	public void removeEventPurchasePolicy(PurchasePolicy pp) {
		purchasePolicy.remove(pp);
	}

	public double getEventPrice() {
		return price; //update when disocunt policies are implemented
	}

	public void setEventPrice(double price) {
		this.price = price;
	}

	public double getEventRating() {
		return rating;
	}

	public LotteryPolicy getLotteryPolicy() {
		return purchasePolicy.stream().filter(pp -> pp instanceof LotteryPolicy).findFirst().map(pp -> ((LotteryPolicy) pp)).orElse(null);
	}

	private void validateName(String name) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Event name cannot be null or empty.");
		}
	}

	private void validateDates(LocalDateTime startTime, LocalDateTime endTime) {
		if (startTime == null || endTime == null) {
			throw new IllegalArgumentException("Start time and end time cannot be null.");
		}
		if (startTime.isAfter(endTime)) {
			throw new IllegalArgumentException("Start time must be before end time.");
		}
		if (endTime.isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("End time must be in the future.");
		}
	}

	private void validateArtist(String artist) {
		if (artist == null || artist.trim().isEmpty()) {
			throw new IllegalArgumentException("Event artist cannot be null or empty.");
		}
	}

	private void validateCategory(String category) {
		if (category == null || category.trim().isEmpty()) {
			throw new IllegalArgumentException("Event category cannot be null or empty.");
		}
	}

	private void validatePrice(double price) {
		if (price < 0) {
			throw new IllegalArgumentException("Event price cannot be negative.");
		}
	}

	private void validateRating(double rating) {
		if (rating < 0 || rating > 5) {
			throw new IllegalArgumentException("Event rating must be between 0 and 5.");
		}
	}

	@Override
	public String toString() {
		return "Event{" +
				"eventID=" + eventID +
				", active=" + active +
				", venueID='" + venueID + '\'' +
				", name='" + name + '\'' +
				", startTime=" + startTime +
				", endTime=" + endTime +
				", artist='" + artist + '\'' +
				", category='" + category + '\'' +
				", productionCompanyID=" + productionCompanyID +
				'}';
	}
}
