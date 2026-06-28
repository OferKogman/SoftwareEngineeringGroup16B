package com.group16b.DomainLayer.Event;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.DomainLayer.Policies.DiscountPolicy.DiscountPolicy;
import com.group16b.DomainLayer.Policies.DiscountPolicy.DiscountPolicySetConverter;
import com.group16b.DomainLayer.Policies.PurchasePolicy.LotteryPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.LotteryPolicyConverter;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicySetConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int eventID;

    @Version 
    private long version;

    @Column(nullable = false)
    private boolean active = false;
    
    @Column(nullable = false)
    private String venueID;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    @Column(nullable = false)
    private LocalDateTime endTime;
    
    private String artist; 
    
    private String category;
    
    @Column(nullable = false)
    private int productionCompanyID; 
    
    @Column(nullable = false)
    private double price;
    
    @Column(nullable = false)
    private double rating;
    
    @Column(nullable = false)
    private String ownerId; 
    
    @Convert(converter = PurchasePolicySetConverter.class)
    @Column(columnDefinition = "TEXT")
    private Set<PurchasePolicy> purchasePolicy = new HashSet<>();

    @Convert(converter = DiscountPolicySetConverter.class)
    @Column(columnDefinition = "TEXT")
    private Set<DiscountPolicy> discountPolicy = new HashSet<>();

    @Convert(converter = LotteryPolicyConverter.class) 
    @Column(columnDefinition = "TEXT")
    private LotteryPolicy lotteryPolicy;

    public Event() {}

	public Event(EventRecord eventRecord, String ownerId) {
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
		this.price = 0; //update only based on segment price
		validateRating(eventRecord.rating());
		this.rating = eventRecord.rating();
		this.ownerId = ownerId;
		this.version = 0;
		lotteryPolicy = null;
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
		this.discountPolicy = copyDiscountPolicies(other.getEventDiscountPolicy());
		this.purchasePolicy = copyPurchasePolicies(other.getEventPurchasePolicy());
		this.price = other.getEventPrice();
		this.rating = other.getEventRating();
		this.ownerId = other.getOwnerId();
		this.version = other.getVersion();
		lotteryPolicy = other.lotteryPolicy == null ? null : new LotteryPolicy(other.getLotteryPolicy());
	}

	private Set<DiscountPolicy> copyDiscountPolicies(Set<DiscountPolicy> policies) {
		Set<DiscountPolicy> copiedPolicies = new HashSet<>();
		for (DiscountPolicy policy : policies) {
			// fix constructor for discount policies
			copiedPolicies.add(policy);
		}
		return copiedPolicies;
	}

	private Set<PurchasePolicy> copyPurchasePolicies(Set<PurchasePolicy> policies) {
		Set<PurchasePolicy> copiedPolicies = new HashSet<>();
		for (PurchasePolicy policy : policies) {
			if (policy instanceof LotteryPolicy lotteryPolicy) {
				copiedPolicies.add(new LotteryPolicy(lotteryPolicy));
			} else {
				// fix constructor for purchase policies
				copiedPolicies.add(policy);
			}
		}
		return copiedPolicies;
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
		return price; // update when disocunt policies are implemented
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

	// lottery
	public LotteryPolicy getLotteryPolicy() throws IllegalStateException {
		if (lotteryPolicy == null) {
			throw new IllegalStateException("Event does not have a lottery policy.");
		}
		return lotteryPolicy;
	}

	public boolean hasLotteryPolicy(){
		return lotteryPolicy!=null;
	}

	public void setLotteryPolicy(LotteryPolicy lotteryPolicy) {
		if (this.lotteryPolicy != null) {
			throw new IllegalStateException("Event already has a lottery policy.");
		}
		this.lotteryPolicy = lotteryPolicy;
	}

	public void validateLotteryCode(String lotteryCode) throws IllegalStateException {
		LotteryPolicy lp = getLotteryPolicy();
		lp.validateLotteryCode(lotteryCode);
	}

	public void renewLotteryCode(String lotteryCode) {
		try {
			LotteryPolicy lp = getLotteryPolicy();
			lp.renewLotteryCode(lotteryCode);
		} catch (Exception e) {
		}
	}

	public void lotteryUseCode(String lotteryCode) throws IllegalStateException {
		LotteryPolicy lp = getLotteryPolicy();
		lp.useCode(lotteryCode);
	}

	public void verifyDoesNotHaveLotteryPolicy() throws IllegalStateException {
		LotteryPolicy lp;
		try {
			lp = getLotteryPolicy();

		} catch (Exception e) {
			return;
		}
		throw new IllegalStateException("Event has a lottery purchase policy.");

	}

	// lottery
	public String getOwnerId() {
		return ownerId;
	}

	public long getVersion() {
		return version;
	}

	public void incrementVersion() {
		this.version++;
	}

	public void handleLotteryResults()
	{
		LotteryPolicy lp=getLotteryPolicy();
		lp.handleLotteryResults();
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
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

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
				productionCompanyID);
	}

	public void enrollInLottery(String userID) {
		if (!this.getEventStatus()) {
			throw new IllegalStateException("Can't enroll in lottery for an inactive event");
		}
		this.getLotteryPolicy().enrollInLottery(this.eventID, userID);
	}

	public void validateEventIsNotEnded() {
		LocalDateTime currentTime = LocalDateTime.now();
		if (endTime.isBefore(currentTime)) {
			throw new IllegalStateException("Event has already ended.");
		}
	}

    public void validateEventPriceIsSet() {
		if (price <= 0) {
			throw new IllegalStateException("Event price must be set and greater than zero.");
		}
    }
}
