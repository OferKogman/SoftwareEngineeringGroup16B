package com.group16b.DomainLayer.Venue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "seats")
public class Seat {
    
    @Id
    @Column(name = "db_id")
    private String dbId;

    @Column(name = "local_seat_id")
    private String seatId;

    private int row;
    private int number;

    @ElementCollection
    @CollectionTable(
        name = "seat_stock", 
        joinColumns = @JoinColumn(name = "seat_db_id") 
    )
    @MapKeyColumn(name = "event_id")      
    @Column(name = "is_reserved")         
    private Map<Integer, Boolean> stock = new HashMap<>();

    public Seat(int row, int number) {
        this.dbId = UUID.randomUUID().toString(); 
        this.seatId = row + "-" + number;
        this.row = row;
        this.number = number;
        this.stock = new HashMap<Integer, Boolean>();
    }

    public Seat() {
        this.dbId = UUID.randomUUID().toString();
        this.seatId = null;
        this.row = 0;
        this.number = 0;
        this.stock = new HashMap<Integer, Boolean>();
    }

    public Seat(Seat other) {
        this.dbId = UUID.randomUUID().toString();
        this.seatId = other.seatId;
        this.row = other.row;
        this.number = other.number;
        this.stock = new HashMap<>(other.stock);
    }

    public String getSeatId() { return seatId; }
    public int getRow() { return row; }
    public int getNumber() { return number; }
    public Map<Integer, Boolean> getStock() { return stock; }

    public boolean getStock(Integer eventID) { return stock.get(eventID); }

    public boolean reserveSeat(int eventID) {
        Boolean reserved = stock.get(eventID);
        if (reserved == null) throw new IllegalArgumentException("this event is not in this venue.");
        if (reserved) return false;
        stock.put(eventID, true);
        return true;
    }

    public boolean isSeatReserved(int eventID) {
        Boolean reserved = stock.get(eventID);
        if (reserved == null) throw new IllegalArgumentException("this event is not in this venue.");
        return reserved;
    }

    public void returnSeat(int eventID) {
        Boolean reserved = stock.get(eventID);
        if (reserved == null) throw new IllegalArgumentException("this event is not in this venue.");
        if (!reserved) throw new IllegalArgumentException("Seat is already free !");
        stock.put(eventID, false);
    }

    public void addEvent(int eventID) {
        stock.putIfAbsent(eventID, false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Seat other)) return false;
        return Objects.equals(dbId, other.dbId);
    }

    @Override
    public int hashCode() { return Objects.hash(dbId); }
}