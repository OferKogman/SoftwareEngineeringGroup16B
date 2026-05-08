package com.group16b.DomainLayer.Event;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.DomainLayer.Policies.DiscountPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy;

public class Event {
	private static int IDCounter = 1;

	private final int eventID;
	private AtomicBoolean active = new AtomicBoolean(false);
	private String venueID;
	private String name;
	private LocalDateTime StarTime;
	private LocalDateTime EndTime;
	private String artist;
	private String category;
	private final int productionCompanyID;
	private DiscountPolicy discountPolicy;
	private PurchasePolicy purchasePolicy;

	public Event(EventRecord eventRecord) {
		this.eventID = IDCounter++;
		this.venueID = eventRecord.venueID();
		validateName(eventRecord.name());
		this.name = eventRecord.name();
		validateDates(eventRecord.startTime(), eventRecord.endTime());
		this.StarTime = eventRecord.startTime();
		this.EndTime = eventRecord.endTime();
		validateArtist(eventRecord.artist());
		this.artist = eventRecord.artist();
		validateCategory(eventRecord.category());
		this.category = eventRecord.category();
		this.productionCompanyID = eventRecord.pcID();
		this.discountPolicy = eventRecord.discountPolicy();
		this.purchasePolicy = eventRecord.purchasePolicy();
	}

	public int getEventID() {
		return eventID;
	}

	public boolean getEventStatus() {
		return active.get();
	}

	public void activateEvent() {
		if(active.getAndSet(true)){
			throw new IllegalStateException("Event is already active.");
		}
	}

	public void deactivateEvent() {
		if(!active.getAndSet(false)){
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
		return StarTime;
	}
	
	public LocalDateTime getEventEndTime() {
		return EndTime;
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

	public int getEventProductionCOmpanyID() {
		return productionCompanyID;
	}

	public DiscountPolicy getEventDiscountPolicy() {
		return discountPolicy;
	}

	public void setEventDiscountPolicy(DiscountPolicy dp) {
		this.discountPolicy = dp;
	}

	public PurchasePolicy getEventPurchasePolicy() {
		return purchasePolicy;
	}

	public void setEventPurchasePolicy(PurchasePolicy pp) {
		this.purchasePolicy = pp;
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
		if(endTime.isBefore(LocalDateTime.now())) {
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
}
