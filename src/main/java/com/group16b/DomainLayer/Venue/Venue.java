package com.group16b.DomainLayer.Venue;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.group16b.ApplicationLayer.Records.ChosenSeatingSegRecord;
import com.group16b.ApplicationLayer.Records.EntranceRecord;
import com.group16b.ApplicationLayer.Records.FieldSegRecord;
import com.group16b.ApplicationLayer.Records.StageRecord;
import com.group16b.ApplicationLayer.Records.VenueGridRecord;
import com.group16b.DomainLayer.Order.OrderType;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKey;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;


@Entity
@Table(name = "venues")
public class Venue {

    @Id
    private String id;
    private int companyID;
    private String name;

    @Version // Automatically tracks updates for safe concurrent writes
    private long version;

    @Embedded
	@AttributeOverride(name = "name", column = @Column(name = "location_name"))
    private Location location;

    @Embedded
    private VenueGrid grid;

    private int IDForSeg = 0;


    // Map Key points directly to the 'segmentID' property inside Segment
    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyColumn(name = "segment_map_key")
    private Map<String, Segment> segments = new HashMap<>();

    // Map Key points directly to the auto-generated database ID inside EventSchedule
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "venue_id")
    @MapKey(name = "dbId")
    private Map<Integer, EventSchedule> scheduledEvents;



    // Maps embeddable components into a separate, clean collection storage table
    @ElementCollection
    @CollectionTable(name = "venue_stages", joinColumns = @JoinColumn(name = "venue_id"))
    @MapKeyColumn(name = "stage_map_key") // Stores the Map's String key
    private Map<String, Stage> stages;

    @ElementCollection
    @CollectionTable(name = "venue_entrances", joinColumns = @JoinColumn(name = "venue_id"))
    @MapKeyColumn(name = "entrance_map_key") // Stores the Map's String key
    private Map<String, Entrance> entrances;

    public Venue(String name, Location location, Map<String, Segment> segments, String id, VenueGrid grid, Map<String, Stage> stages, Map<String, Entrance> entrances, int companyID) {
        this.name = name;
        this.id = id;
        this.location = location;
        this.segments = new HashMap<>();
        this.scheduledEvents = new HashMap<>();
        this.grid = grid;
        this.stages = stages;
        this.entrances = entrances;
        this.companyID = companyID;

        for (Segment segment : segments.values()) {
            addSegment(segment);
        }
    }

	public Venue(String name, Location location, List<FieldSegRecord> fieldSeg, List<ChosenSeatingSegRecord> seatSeg, String id, VenueGridRecord grid, List<StageRecord> stages, List<EntranceRecord> entrances, int companyID) {
        this.name = name;
        this.location = location;
        this.id = id;
        this.segments = new HashMap<>();
        for (FieldSegRecord fsr : fieldSeg) {
            addSegment(new FieldSeg(fsr.segmentID(), fsr.size(), new GridRectangle(fsr.area())));
        }
        for (ChosenSeatingSegRecord cssr : seatSeg) {
            addSegment(new ChosenSeatingSeg(cssr.segmentID(), cssr.seats(), new GridRectangle(cssr.area())));
        }
        this.scheduledEvents = new HashMap<>();
        this.grid = new VenueGrid(grid.rows(), grid.columns());
        this.stages = new HashMap<>();
        for(StageRecord record: stages){
            this.stages.put(record.stageID(), new Stage(record.stageID(), new GridRectangle(record.area())));
        }
        this.entrances = new HashMap<>();
        for(EntranceRecord record: entrances){
            this.entrances.put(record.entranceID(), new Entrance(record.entranceID(), new GridRectangle(record.area())));
        }
        this.companyID = companyID;
    }
	protected Venue() {
		this.location = null;
		this.segments = null;
		this.stages = null;
		this.entrances = null;
		this.scheduledEvents = null;
		this.id = null;
        this.companyID = 0;
	}
	
	public Venue(Venue other) {
        this.id = other.getID();
        this.name = other.getName();
        this.location = other.getLocation();

        this.segments = new HashMap<>();
        for (Map.Entry<String, Segment> entry : other.getSegments().entrySet()) {
            Segment origSeg = entry.getValue();
            if (origSeg instanceof FieldSeg fieldSeg) {
                addSegment(new FieldSeg(fieldSeg)); 
            } else if (origSeg instanceof ChosenSeatingSeg seatingSeg) {
                addSegment(new ChosenSeatingSeg(seatingSeg));
            }
        }
        

        this.scheduledEvents = new HashMap<>();
        for (Map.Entry<Integer, EventSchedule> entry : other.getScheduledEvents().entrySet()) {
            this.scheduledEvents.put(entry.getKey(), new EventSchedule(entry.getValue().getStartTime(), entry.getValue().getEndTime())); 
        }

        this.grid = new VenueGrid(other.getGrid().getRows(), other.getGrid().getColumns());
        this.stages = new HashMap<>(other.getStages());
        this.entrances = new HashMap<>(other.getEntrances());

        this.IDForSeg = other.getIDForSeg();
        this.version = other.getVersion();
        this.companyID = other.getCompanyID();
    }

	private int getIDForSeg(){
		return IDForSeg;
	}
    public int getCompanyID() {
        return companyID;
    }

	public Map<String, Segment> getSegments(){
		return segments;
	}
    public void addSeatSegment(String segmentID, Map<String, Seat> seats, GridRectangle area) {
        if (segments.containsKey(segmentID)) {
            throw new IllegalArgumentException("Segment with ID " + segmentID + " already exists in the venue!");
        }
        addSegment(new ChosenSeatingSeg(segmentID, seats, area));
    }
    public void addFieldSegment(String segmentID, int size, GridRectangle area) {
        if (segments.containsKey(segmentID)) {
            throw new IllegalArgumentException("Segment with ID " + segmentID + " already exists in the venue!");
        }
        addSegment(new FieldSeg(segmentID, size, area));
    }
        

    public boolean hasSegment(String segmentID) {
        return segments.containsKey(segmentID);
    }

	public Map<Integer, EventSchedule> getScheduledEvents(){
		return scheduledEvents;
	}

	public String getName() {
		return name;
	}

	public void setName(String newName) {
		name = newName;
	}

	public Location getLocation() {
		return location;
	}

	public Segment getSegmentByID(String id) {
		return segments.get(id);
	}
    public String getSegmentTypeByID(String segmentID) {
        Segment segment = segments.get(segmentID);
        if (segment == null) {
            throw new IllegalArgumentException("Segment with ID " + segmentID + " not found");
        }
        return segment.getSegmentType();
    }
    public int getReservedStockBySegmentEventField(int eventID, String segmentID) {
        Segment segment = segments.get(segmentID);
        if (segment == null) {
            throw new IllegalArgumentException("Segment with ID " + segmentID + " not found");
        }
        if (segment instanceof FieldSeg fieldSeg) {
            return fieldSeg.getFieldSize() - fieldSeg.getStock(eventID); // amount already sold
        }
        throw new IllegalArgumentException("Segment with ID " + segmentID + " is not a field segment");
    }
    public void setStockForEvent(int eventID, Map.Entry<String, Integer> fieldSegments) {
        Segment currSeg = getSegmentByID(fieldSegments.getKey());
        if (currSeg == null) {
            throw new IllegalArgumentException("Segment with ID: " + fieldSegments.getKey() + " not found in venue !");
        }
        if (!currSeg.getSegmentType().equals("F")) {
            throw new IllegalArgumentException("Segment with ID: " + fieldSegments.getKey() + " is not a field segment !");
        }
        FieldSeg fieldSeg = (FieldSeg) currSeg;
        fieldSeg.setStockForEvent(eventID, fieldSegments.getValue());
    }
    public List<String> getStockRefundForEvent(int eventID,String segmentID, List<String> newSeatsIDs) {
        Segment segment = segments.get(segmentID);
        if (segment == null) {
            throw new IllegalArgumentException("Segment with ID " + segmentID + " not found");
        }
        if (segment instanceof ChosenSeatingSeg seatingSeg) {
            return seatingSeg.getStockRefundForEvent(eventID, newSeatsIDs);
        }
        throw new IllegalArgumentException("Segment with ID " + segmentID + " is not a seat segment");
    }
	public Integer getEventIDByLocalDateTime(LocalDateTime targetDate) {
        if (targetDate == null) return null;

        for (Map.Entry<Integer, EventSchedule> entry : scheduledEvents.entrySet()) {
            EventSchedule schedule = entry.getValue();
            
            if (!targetDate.isBefore(schedule.getStartTime()) && !targetDate.isAfter(schedule.getEndTime())) {
                return entry.getKey();
            }
        }
        
        // no event is happening at this specific time
        return null; 
    }

	public EventSchedule getEventSchedule(int eventID) {
        return scheduledEvents.get(eventID);
    }


	public synchronized void bookEvent(LocalDateTime startTime, LocalDateTime endTime, int eventID) {
		if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Event start time must be before end time!");
        }

		// initialize stock
		// use start time n end time for event
		if (scheduledEvents.containsKey(eventID)) {
            throw new IllegalArgumentException("Venue is already reserved for this event ID!");
        }

        EventSchedule newSchedule = new EventSchedule(startTime, endTime);
        for (EventSchedule existingSchedule : scheduledEvents.values()) {
            if (newSchedule.overlapsWith(existingSchedule)) {
                throw new IllegalArgumentException(
                    "Venue is already booked during this time frame! Conflicts with an existing event."
                );
            }
        }

        scheduledEvents.put(eventID, new EventSchedule(startTime, endTime));

        for (Segment segment : segments.values()) {
            if (segment instanceof FieldSeg fieldSeg) {
                fieldSeg.addEvent(eventID);
            } else if (segment instanceof ChosenSeatingSeg seatingSeg) {
                for (Seat seat : seatingSeg.seats.values()) {
                    seat.addEvent(eventID);
                }
            }
        }
    }
	public OrderType segmentType(String segmentId) {
		Segment segment = segments.get(segmentId);
		if (segment == null) {
			throw new IllegalArgumentException("Segment with ID " + segmentId + " not found");
		}
		if (segment instanceof ChosenSeatingSeg) {
			return OrderType.SEAT;
		} else if (segment instanceof FieldSeg) {
			return OrderType.FIELD;
		} else {
			throw new IllegalArgumentException("Unknown segment type for segment ID " + segmentId);
		}
	}
	
	public void cancelEvent(LocalDateTime starTime, int eventID) {
		if (scheduledEvents.remove(eventID) == null) {
			throw new IllegalArgumentException("Venue is not reserved for this event at requested date !");
		}
	}
	public void cancelSeatReservation(String segmentId, List<String> seatIds, int eventID) {
		this.freeTickets(segmentId, seatIds, 0, eventID);
	}
	public void cancelFieldReservation(String segmentId, int quantity, int eventID) {
		this.freeTickets(segmentId, null, quantity, eventID);
	}
	private void freeTickets(String segmentId, List<String> seatIds, int quantity, int eventID) {
		Segment segment = segments.get(segmentId);
		if (segment == null) {
			throw new IllegalArgumentException("Segment with ID " + segmentId + " not found");
		}
		if (segment instanceof ChosenSeatingSeg) {
			this.freeSeats(ReservationRequest.forSeats(eventID, seatIds, segmentId));
		} else if (segment instanceof FieldSeg) {
			this.freeSeats(ReservationRequest.forField(eventID, quantity, segmentId));
		} else {
			throw new IllegalArgumentException("Unknown segment type for segment ID " + segmentId);
		}
	}

    public void reserveTickets(String segmentId, List<String> seatIds, int eventID) {
        this.reserveSeats(ReservationRequest.forSeats(eventID, seatIds, segmentId));
    }

    public void reserveTickets(String segmentId, int quantity, int eventID) {
        this.reserveSeats(ReservationRequest.forField(eventID, quantity, segmentId));
    }

	public void reserveSeats(ReservationRequest request) {
		Segment segment = segments.get(request.getSegmentId());
		if (segment == null) {
			throw new IllegalArgumentException("Segment with ID " + request.getSegmentId() + " not found");
		}
		segment.reserve(request);
	}
	
	public void freeSeats(ReservationRequest request) {
		Segment segment = segments.get(request.getSegmentId());
		if (segment == null) {
			throw new IllegalArgumentException("Segment with ID " + request.getSegmentId() + " not found");
		}
		segment.cancelReservation(request);
	}

	public double getPriceForSegment(String segmentId, int eventID) {
		Segment segment = segments.get(segmentId);
		if (segment == null) {
			throw new IllegalArgumentException("Segment with ID " + segmentId + " not found");
		}
		return segment.getPrice(eventID);
	}
    public void addPriceToSegment(String segmentId, double price, int eventID) {
        Segment segment = segments.get(segmentId);
        if (segment == null) {
            throw new IllegalArgumentException("Segment with ID " + segmentId + " not found");
        }
        segment.setPrice(eventID, price);
    }
	
	public String getID(){
		return id;
	}

	public long getVersion(){
		return version;
	}

	public void setVersion(long version){
		this.version = version;
	}

    public void setNewSeatingStock(String segmentId, List<String> newSeatsIDs) {
        Segment segment = segments.get(segmentId);
        if (segment == null) {
            throw new IllegalArgumentException("Segment with ID " + segmentId + " not found");
        }
        if (segment instanceof ChosenSeatingSeg chosenSeatingSeg) {
            chosenSeatingSeg.setNewStock(newSeatsIDs);
        } else {
            throw new IllegalArgumentException("Segment with ID " + segmentId + " is not a chosen seating segment");
        }
    }

    public void setNewFieldStock(String segmentId, int newSize, List<Integer> eventIDsToUpdate) {

        Segment segment = segments.get(segmentId);

        if (segment == null) {
            throw new IllegalArgumentException("Segment with ID " + segmentId + " not found");
        }

        if (!(segment instanceof FieldSeg fieldSeg)) {
            throw new IllegalArgumentException("Segment with ID " + segmentId + " is not a field segment");
        }

        int oldSize = fieldSeg.getFieldSize();

        for (Integer eventID : eventIDsToUpdate) {
            int oldFreeStock = fieldSeg.getStock(eventID);
            int reserved = oldSize - oldFreeStock;

            if (reserved > newSize) {
                throw new IllegalArgumentException(
                        "Cannot set field segment " + segmentId +
                        " size to " + newSize +
                        " because event " + eventID +
                        " has " + reserved + " reserved tickets."
                );
            }

            fieldSeg.setStockForEvent(eventID, Math.max(0, newSize - reserved));
        }

        fieldSeg.setSize(newSize);
    }
    public void setNewFieldStockAfterRefunds(
            String segmentId,
            int newSize,
            int eventID,
            int finalReserved
    ) {
        Segment segment = segments.get(segmentId);

        if (!(segment instanceof FieldSeg fieldSeg)) {
            throw new IllegalArgumentException("Segment is not a field segment");
        }

        if (finalReserved > newSize) {
            throw new IllegalArgumentException("Still too many reserved tickets after refunds");
        }

        fieldSeg.setSize(newSize);
        fieldSeg.setStockForEvent(eventID, newSize - finalReserved);
    }
    public void addChosenSeatingSegment(ChosenSeatingSegRecord seatSeg) {
        addSegment(new ChosenSeatingSeg(seatSeg.segmentID(), seatSeg.seats(), new GridRectangle(seatSeg.area())));
    }
    public void addFieldSegment(FieldSegRecord fieldSeg) {
        addSegment(new FieldSeg(fieldSeg.segmentID(), fieldSeg.size(), new GridRectangle(fieldSeg.area())));
    }
    public void validateCompanyID(int companyID) {
        if (this.companyID != companyID) {
            throw new IllegalArgumentException("Company ID mismatch: Venue does not belong to the specified company.");
        }
    }
    

    public void removeSegment(String segmentID) {
        if (!segments.containsKey(segmentID)) {
            throw new IllegalArgumentException("Segment with ID " + segmentID + " not found");
        }

        segments.remove(segmentID);
    }

    public void replaceStages(List<StageRecord> stageRecords) {
        stages.clear();

        for (StageRecord record : stageRecords) {
            stages.put(record.stageID(), new Stage(record.stageID(), new GridRectangle(record.area())));
        }
    }

    public void replaceEntrances(List<EntranceRecord> entranceRecords) {
        entrances.clear();

        for (EntranceRecord record : entranceRecords) {
            entrances.put(record.entranceID(), new Entrance(record.entranceID(), new GridRectangle(record.area())));
        }
    }

    public void replaceGrid(VenueGridRecord gridRecord) {
        this.grid = new VenueGrid(gridRecord.rows(), gridRecord.columns());
    }
    public void initializeSegmentForEvent(String segmentID, int eventID) {
        Segment segment = segments.get(segmentID);

        if (segment == null) {
            throw new IllegalArgumentException("Segment with ID " + segmentID + " not found");
        }

        if (segment instanceof FieldSeg fieldSeg) {
            fieldSeg.addEvent(eventID);
            return;
        }

        if (segment instanceof ChosenSeatingSeg seatingSeg) {
            for (Seat seat : seatingSeg.seats.values()) {
                seat.addEvent(eventID);
            }
            return;
        }

        throw new IllegalArgumentException("Unknown segment type for segment ID " + segmentID);
    }
    public void setNewSeatingStock(
            String segmentId,
            List<String> newSeatsIDs,
            List<Integer> eventIDsToInitialize
    ) {
        Segment segment = segments.get(segmentId);

        if (segment == null) {
            throw new IllegalArgumentException("Segment with ID " + segmentId + " not found");
        }

        if (segment instanceof ChosenSeatingSeg chosenSeatingSeg) {
            chosenSeatingSeg.setNewStock(newSeatsIDs, eventIDsToInitialize);
            return;
        }

        throw new IllegalArgumentException("Segment with ID " + segmentId + " is not a chosen seating segment");
    }

	public VenueGrid getGrid() { return grid; }
    public Map<String, Stage> getStages() { return stages; }
    public Map<String, Entrance> getEntrances() { return entrances; }

    private void addSegment(Segment segment) {
        segment.setVenue(this);
        this.segments.put(segment.getSegmentID(), segment);
    }
}
