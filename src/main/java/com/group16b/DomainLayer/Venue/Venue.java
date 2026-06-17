package com.group16b.DomainLayer.Venue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private final String id;

    private volatile String name;

    @Version // Automatically tracks updates for safe concurrent writes
    private long version;

    @Embedded
	@AttributeOverride(name = "name", column = @Column(name = "location_name"))
    private final Location location;

    @Embedded
    private VenueGrid grid;

    private int IDForSeg = 0;


    // Map Key points directly to the 'segmentID' property inside Segment
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "venue_id")
    @MapKey(name = "segmentID")
    private final Map<String, Segment> segments;

    // Map Key points directly to the auto-generated database ID inside EventSchedule
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "venue_id")
    @MapKey(name = "dbId")
    private final Map<Integer, EventSchedule> scheduledEvents;



    // Maps embeddable components into a separate, clean collection storage table
    @ElementCollection
    @CollectionTable(name = "venue_stages", joinColumns = @JoinColumn(name = "venue_id"))
    @MapKeyColumn(name = "stage_map_key") // Stores the Map's String key
    private final Map<String, Stage> stages;

    @ElementCollection
    @CollectionTable(name = "venue_entrances", joinColumns = @JoinColumn(name = "venue_id"))
    @MapKeyColumn(name = "entrance_map_key") // Stores the Map's String key
    private final Map<String, Entrance> entrances;

	public Venue(String name, Location location, Map<String, Segment> segments, String id, VenueGrid grid, Map<String, Stage> stages, Map<String, Entrance> entrances) {
        this.name = name;
        this.location = location;
        this.segments = segments;
        this.scheduledEvents = new ConcurrentHashMap<>();
        this.grid = grid;
        this.stages = stages;
        this.entrances = entrances;
        this.id = id;
    }

	public Venue(String name, Location location, List<FieldSegRecord> fieldSeg, List<ChosenSeatingSegRecord> seatSeg, String id, VenueGridRecord grid, List<StageRecord> stages, List<EntranceRecord> entrances) {
        this.name = name;
        this.location = location;
        this.segments = new ConcurrentHashMap<>();
        for (FieldSegRecord fsr : fieldSeg) {
            this.segments.put(fsr.segmentID(), new FieldSeg(fsr.segmentID(), fsr.size(), new GridRectangle(fsr.area())));
        }
        for (ChosenSeatingSegRecord cssr : seatSeg) {
            this.segments.put(cssr.segmentID(), new ChosenSeatingSeg(cssr.segmentID(), cssr.seats(), new GridRectangle(cssr.area())));
        }
        this.scheduledEvents = new ConcurrentHashMap<>();
        this.grid = new VenueGrid(grid.rows(), grid.columns());
        this.stages = new ConcurrentHashMap<>();
        for(StageRecord record: stages){
            this.stages.put(record.stageID(), new Stage(record.stageID(), new GridRectangle(record.area())));
        }
        this.entrances = new ConcurrentHashMap<>();
        for(EntranceRecord record: entrances){
            this.entrances.put(record.entranceID(), new Entrance(record.entranceID(), new GridRectangle(record.area())));
        }
        this.id = id;
    }
	protected Venue() {
		this.location = null;
		this.segments = null;
		this.stages = null;
		this.entrances = null;
		this.scheduledEvents = null;
		this.id = null;
	}
	
	public Venue(Venue other) {
        this.name = other.getName();
        this.location = other.getLocation();

        this.segments = new ConcurrentHashMap<>();
        for (Map.Entry<String, Segment> entry : other.getSegments().entrySet()) {
            Segment origSeg = entry.getValue();
            if (origSeg instanceof FieldSeg fieldSeg) {
                this.segments.put(entry.getKey(), new FieldSeg(fieldSeg)); 
            } else if (origSeg instanceof ChosenSeatingSeg seatingSeg) {
                this.segments.put(entry.getKey(), new ChosenSeatingSeg(seatingSeg));
            }
        }

        this.scheduledEvents = new ConcurrentHashMap<>();
        for (Map.Entry<Integer, EventSchedule> entry : other.getScheduledEvents().entrySet()) {
            this.scheduledEvents.put(entry.getKey(), new EventSchedule(entry.getValue().getStartTime(), entry.getValue().getEndTime())); 
        }

        this.grid = other.getGrid();
        this.stages = new ConcurrentHashMap<>(other.getStages());
        this.entrances = new ConcurrentHashMap<>(other.getEntrances());

        this.IDForSeg = other.getIDForSeg();
        this.version = other.getVersion();
        this.id = other.getID();
    }

	private int getIDForSeg(){
		return IDForSeg;
	}

	public Map<String, Segment> getSegments(){
		return segments;
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
	
	public String getID(){
		return id;
	}

	public long getVersion(){
		return version;
	}

	public void setVersion(long version){
		this.version = version;
	}

	public VenueGrid getGrid() { return grid; }
    public Map<String, Stage> getStages() { return stages; }
    public Map<String, Entrance> getEntrances() { return entrances; }
}
