package com.group16b.DomainLayer.Venue;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Inheritance(strategy = jakarta.persistence.InheritanceType.JOINED)
@Table(name = "segments")
abstract public class Segment {

    @EmbeddedId
    protected SegmentId id;

    @MapsId("venueId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", referencedColumnName = "id")
    protected Venue venue;

    @ElementCollection
    @CollectionTable(
        name = "segment_event_prices",
        joinColumns = {
            @JoinColumn(name = "venue_id", referencedColumnName = "venue_id"),
            @JoinColumn(name = "segment_id", referencedColumnName = "segment_id")
        }
    )
    @MapKeyColumn(name = "event_id")
    @Column(name = "price")
    protected Map<Integer, Double> eventPrices = new HashMap<>();

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "startRow", column = @Column(name = "seg_startRow")),
			@AttributeOverride(name = "startColumn", column = @Column(name = "seg_startColumn")),
			@AttributeOverride(name = "rowCount", column = @Column(name = "seg_rowCount")),
			@AttributeOverride(name = "columnCount", column = @Column(name = "seg_columnCount"))
	})
	protected GridRectangle area;

	protected Segment() {
    	// JPA
	}
	public Segment(String segmentID, GridRectangle area) {
    this.id = new SegmentId(null, segmentID);
    this.area = area;
	}

	public Segment(Segment other) {
		this.id = new SegmentId(null, other.getSegmentID());
		this.area = other.area;
		this.eventPrices = new HashMap<>(other.eventPrices);
	}

	public String getSegmentID() {
		return id != null ? id.getSegmentId() : null;
	}

	public String getVenueID() {
		return id != null ? id.getVenueId() : null;
	}

	public void setVenue(Venue venue) {
		this.venue = venue;

		if (this.id == null) {
			this.id = new SegmentId();
		}

		this.id.setVenueId(venue.getID());
	}

	public GridRectangle getArea() {
		return area;
	}

	public Map<Integer, Double> getEventPrices() {
		return eventPrices;
	}

	public double getPrice(int eventID) {
		validateEventId(eventID);
		return eventPrices.getOrDefault(eventID, 0.0);
	}

	public void setPrice(int eventID, double price) {
		validateEventId(eventID);
		if (price <= 0)
			throw new IllegalArgumentException("price must be positive");
		eventPrices.put(eventID, price);
	}

	public abstract void reserve(ReservationRequest request);

	public abstract void cancelReservation(ReservationRequest request);

	public abstract String getSegmentType();

	public abstract Map<?, ?> getMap();

	protected void validateEventId(int eventID) {
		if (eventID <= 0)
			throw new IllegalArgumentException("Invalid event id: " + eventID);
	}

}
