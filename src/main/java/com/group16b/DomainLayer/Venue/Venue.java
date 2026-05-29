package com.group16b.DomainLayer.Venue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.group16b.ApplicationLayer.Records.ChosenSeatingSegRecord;
import com.group16b.ApplicationLayer.Records.FieldSegRecord;

public class Venue {
	private volatile String name;
	private final Location location;
	private final Map<String, Segment> segments;
	private final Map<Integer, EventSchedule> scheduledEvents;
	private int IDForSeg = 0;
	private long version;
	private final String id;

	public Venue(String name, Location location, Map<String, Segment> segments, String id) {
		this.name = name;
		this.location = location;
		this.segments = segments;
		this.scheduledEvents = new ConcurrentHashMap<>();
		this.id = id;
	}

	public Venue(String name, Location location, List<FieldSegRecord> fieldSeg, List<ChosenSeatingSegRecord> seatSeg, String id) {
		this.name = name;
		this.location = location;
		this.segments = new ConcurrentHashMap<>();
		for (FieldSegRecord fsr : fieldSeg) {
			segments.put(fsr.segmentID(), new FieldSeg(fsr.segmentID(), fsr.size()));
		}
		for (ChosenSeatingSegRecord cssr : seatSeg) {
			segments.put(cssr.segmentID(), new ChosenSeatingSeg(cssr.segmentID(), cssr.seats()));
		}
		this.scheduledEvents = new ConcurrentHashMap<>();
		this.id = id;
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
	
	public void cancelEvent(LocalDateTime starTime, int eventID) {
		if (scheduledEvents.remove(eventID) == null) {
			throw new IllegalArgumentException("Venue is not reserved for this event at requested date !");
		}
	}

	public void freeTickets(String segmentId, List<String> seatIds, int eventID){
		this.freeSeats(ReservationRequest.forSeats(eventID, seatIds, segmentId));
	}

	public void freeTickets(String segmentId, int quantity, int eventID){
		this.freeSeats(ReservationRequest.forField(eventID, quantity, segmentId));
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
	
	public String getID(){
		return id;
	}

	public long getVersion(){
		return version;
	}

	public void setVersion(long version){
		this.version = version;
	}
}
