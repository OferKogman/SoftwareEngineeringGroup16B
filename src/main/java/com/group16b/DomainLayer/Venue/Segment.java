package com.group16b.DomainLayer.Venue;

import java.util.Map;

import org.springframework.context.annotation.EnableAspectJAutoProxy;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.Table;
@Entity
@Inheritance(strategy = jakarta.persistence.InheritanceType.JOINED)
@Table(name = "segments")
abstract public class Segment {
	@Id
    protected String segmentID;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name="startRow", column=@Column(name="seg_startRow")),
        @AttributeOverride(name="startColumn", column=@Column(name="seg_startColumn")),
        @AttributeOverride(name="rowCount", column=@Column(name="seg_rowCount")),
        @AttributeOverride(name="columnCount", column=@Column(name="seg_columnCount"))
    })
    protected GridRectangle area;

	public Segment(String segmentID, GridRectangle area) {
		this.segmentID = segmentID;
		this.area = area;
	}

	public Segment() {
		// Default constructor for JPA
	}
	public String getSegmentID() {
		return segmentID;
	}

	public abstract void reserve(ReservationRequest request);
	public abstract void cancelReservation(ReservationRequest request);

	public abstract String getSegmentType();
	public abstract double getPrice(int eventID);
	public abstract void setStockForEvent(int eventID, int stock);
	public abstract Map<?, ?> getMap();
}
