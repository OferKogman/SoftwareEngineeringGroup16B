package com.group16b.DomainLayer.Venue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "seats")
public class Seat {

    @EmbeddedId
    private SeatId id;

    @MapsId("segmentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segment_id", referencedColumnName = "segmentID")
    private ChosenSeatingSeg segment;

    @Column(name = "seat_row")
    private int row;

    @Column(name = "seat_number")
    private int number;

    @ElementCollection
    @CollectionTable(
        name = "seat_stock",
        joinColumns = {
            @JoinColumn(name = "seat_id", referencedColumnName = "seat_id"),
            @JoinColumn(name = "segment_id", referencedColumnName = "segment_id")
        }
    )
    @MapKeyColumn(name = "event_id")
    @Column(name = "is_reserved")
    private Map<Integer, Boolean> stock = new HashMap<>();


	public Seat(int row, int number) {
		String seatId = row + "-" + number;
		this.id = new SeatId(seatId, null);
		this.row = row;
		this.number = number;
		this.stock = new HashMap<>();
	}

	public Seat() {
		this.stock = new HashMap<>();
	}

	public Seat(Seat other) {
		this.id = new SeatId(other.getSeatId(), null);
		this.row = other.row;
		this.number = other.number;
		this.stock = new HashMap<>(other.stock);
	}

	public SeatId getId() {
		return id;
	}

	public String getSeatId() {
		return id != null ? id.getSeatId() : null;
	}

	public String getSegmentId() {
		return id != null ? id.getSegmentId() : null;
	}

	public ChosenSeatingSeg getSegment() {
		return segment;
	}

	public void setSegment(ChosenSeatingSeg segment) {
		this.segment = segment;

		if (this.id == null) {
			this.id = new SeatId();
		}

		this.id.setSeatId(row + "-" + number);
		this.id.setSegmentId(segment.getSegmentID());
	}


	public int getRow() {
		return row;
	}

	public int getNumber() {
		return number;
	}

	public boolean getStock(Integer eventID) {
		return stock.get(eventID);
	}

	public boolean reserveSeat(int eventID) {
		Boolean reserved = stock.get(eventID);
		if (reserved == null) {
			throw new IllegalArgumentException("this event is not in this venue.");
		}
		if (reserved) {
			return false;
		}
		stock.put(eventID, true);
		return true;
	}

	public boolean isSeatReserved(int eventID) {
		Boolean reserved = stock.get(eventID);
		if (reserved == null) {
			throw new IllegalArgumentException("this event is not in this venue.");
		}
		return reserved;
	}

	public void returnSeat(int eventID) {
		Boolean reserved = stock.get(eventID);
		if (reserved == null) {
			throw new IllegalArgumentException("this event is not in this venue.");
		}
		if (!reserved) {
			throw new IllegalArgumentException("Seat is already free !");
		}
		stock.put(eventID, false);
	}

	public void addEvent(int eventID) {
		stock.putIfAbsent(eventID, false);
	}


	public Map<Integer, Boolean> getStock() {
		return stock;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Seat other)) return false;
		return Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}