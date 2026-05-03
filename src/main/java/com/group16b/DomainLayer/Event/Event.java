package com.group16b.DomainLayer.Event;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import com.group16b.DomainLayer.Policies.DiscountPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy;

class Event {
    private static int IDCounter = 1;

    private final int eventID;
    private AtomicBoolean active = new AtomicBoolean(false);
    private Venue venue;
    private String name;
    private Date date;
    private String artist;
    private String category;
    private final int productionCompanyID;
    private DiscountPolicy discountPolicy;
    private PurchasePolicy purchasePolicy;

    public Event(Venue venue, String name, Date date, String artist, String category, int pcID) {
        this.eventID = IDCounter++;
        this.venue = venue;
        this.venue.addEvent(date, eventID);
        // initialize stock
        this.name = name;
        this.date = date;
        this.artist = artist;
        this.category = category;
        this.productionCompanyID = pcID;
    }

    protected int getEventID() {
        return eventID;
    }

    protected boolean getEventStatus() {
        return active.get();
    }

    protected void activateEvent() {
        active.set(true);
    }

    protected void deactivateEvent() {
        active.set(false);
    }

    protected Venue getEventVenue() {
        return venue;
    }

    protected void setEventVenue(Venue venue) {
        this.venue = venue;
        // initialize stock and handle old sales
    }

    protected String getEventName() {
        return name;
    }

    protected void setEventName(String name) {
        this.name = name;
    }

    protected Date getEventDate() {
        return date;
    }

    protected void setEventDate(Date date) {
        this.date = date;
        // update all stock
    }

    protected String getEventArtist() {
        return artist;
    }

    protected void setEventArtist(String artist) {
        this.artist = artist;
    }

    protected String getEventCategory() {
        return category;
    }

    protected void setEventCategory(String category) {
        this.category = category;
    }

    protected int getEventProductionCOmpanyID() {
        return productionCompanyID;
    }

    protected DiscountPolicy getEventDiscountPolicy() {
        return discountPolicy;
    }

    protected void setEventDiscountPolicy(DiscountPolicy dp) {
        this.discountPolicy = dp;
    }

    protected PurchasePolicy getEvenPurchasePolicy() {
        return purchasePolicy;
    }

    protected void setEventPurchasePolicy(PurchasePolicy pp) {
        this.purchasePolicy = pp;
    }
}
