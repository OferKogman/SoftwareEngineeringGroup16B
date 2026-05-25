package com.group16b.DomainLayer.Event;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.DomainLayer.Policies.DiscountPolicy.DiscountPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.LotteryPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;

public class Event {
	private static int IDCounter = 1;

	private final int eventID;
	private boolean active = false;
	private String venueID;
	private String name;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private String artist;
	private String category;
	private final int productionCompanyID;
	private final Set<DiscountPolicy> discountPolicy;
	private final Set<PurchasePolicy> purchasePolicy;
	private double price;
	private double rating;
	private final String ownerId;

	private long version;

	public Event(EventRecord eventRecord, String ownerId) {
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
		this.discountPolicy = new HashSet<>();
		this.purchasePolicy = new HashSet<>();
		validatePrice(eventRecord.price());
		this.price = eventRecord.price();
		validateRating(eventRecord.rating());
		this.rating = eventRecord.rating();
		this.ownerId = ownerId;
		this.version = 0;
	}

	public Event(Event other) {
		this.eventID = other.getEventID();
		this.active = other.getEventStatus();
		this.venueID = other.getEventVenueID();
		this.name = other.getEventName();
		this.startTime = other.getEventStartTime();
		this.endTime = other.getEventEndTime();
		this.artist = other.getEventArtist();
		this.category = other.getEventCategory();
		this.productionCompanyID = other.getEventProductionCompanyID();
		this.discountPolicy = new HashSet<>(other.getEventDiscountPolicy());
		this.purchasePolicy = new HashSet<>(other.getEventPurchasePolicy());
		this.price = other.getEventPrice();
		this.rating = other.getEventRating();
		this.ownerId = other.getOwnerId();
		this.version = other.getVersion();
	}

	public int getEventID() {
		return eventID;
	}

	public boolean getEventStatus() {
		return active;
	}

	public void activateEvent() {
		if (active) {
			throw new IllegalStateException("Event is already active.");
		}
		active = true;
	}
	public void validateEventIsActive() {
		if (!active) {
			throw new IllegalStateException("Event is inactive.");
		}
	}

	public void deactivateEvent() {
		if (!active) {
			throw new IllegalStateException("Event is already inactive.");
		}
		active = false;
	}

	public String getEventVenueID() {
		return venueID;
	}

	public void setEventVenue(String venueID) {
		this.venueID = venueID;
	}

	public String getEventName() {
		return name;
	}

	public void setEventName(String name) {
		validateName(name);
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
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public String getEventArtist() {
		return artist;
	}

	public void setEventArtist(String artist) {
		validateArtist(artist);
		this.artist = artist;
	}

	public String getEventCategory() {
		return category;
	}

	public void setEventCategory(String category) {
		validateCategory(category);
		this.category = category;
	}

	public int getEventProductionCompanyID() {
		return productionCompanyID;
	}

	public Set<DiscountPolicy> getEventDiscountPolicy() {
		return new HashSet<>(discountPolicy);
	}

	public void addEventDiscountPolicy(DiscountPolicy dp) {
		discountPolicy.add(dp);
	}

	public void removeEventDiscountPolicy(DiscountPolicy dp) {
		discountPolicy.remove(dp);
	}

	public Set<PurchasePolicy> getEventPurchasePolicy() {
        return new HashSet<>(purchasePolicy);
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
		validatePrice(price);
		this.price = price;
	}

	public double getEventRating() {
		return rating;
	}

	public void setEventRating(double rating) {
		validateRating(rating);
		this.rating = rating;
	}

	public LotteryPolicy getLotteryPolicy() {
		LotteryPolicy lp = purchasePolicy.stream().filter(pp -> pp instanceof LotteryPolicy).findFirst().map(pp -> ((LotteryPolicy) pp)).orElse(null);
		//if (lp == null) {
		//	throw new IllegalStateException("Event does not have a lottery policy.");
		//}
		return lp;
	}
	
	public String getOwnerId() {
		return ownerId;
	}

	public long getVersion() {
		return version;
	}

	public void incrementVersion() {
		this.version++;
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

	@Override
	public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        return eventID == event.eventID &&
                active == event.active &&
                productionCompanyID == event.productionCompanyID &&
                Objects.equals(venueID, event.venueID) &&
                Objects.equals(name, event.name) &&
                Objects.equals(startTime, event.startTime) &&
                Objects.equals(endTime, event.endTime) &&
                Objects.equals(artist, event.artist) &&
                Objects.equals(category, event.category);
    }


	@Override
    public int hashCode() {
        return Objects.hash(
                eventID,
                active,
                venueID,
                name,
                startTime,
                endTime,
                artist,
                category,
                productionCompanyID
        );
    }


	public boolean isActiveEvent() {//relevant for venue assignment
        return active; 
    }

	public void enrollInLottery(String userID) {
		if (!this.isActiveEvent()) {
			throw new IllegalStateException("Can't enroll in lottery for an inactive event");
		}
		this.getLotteryPolicy().enrollInLottery(this.eventID, userID);
	}
}
