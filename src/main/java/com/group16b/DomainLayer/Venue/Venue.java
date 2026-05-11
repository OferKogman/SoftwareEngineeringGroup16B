package com.group16b.DomainLayer.Venue;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Venue {
	private volatile String name;
	private final Location location;
	private final Map<String, Segment> segments;
	private final Map<Integer, EventSchedule> scheduledEvents;

	protected Venue(String name, Location location, Map<String, Segment> segments) {
		this.name = name;
		this.location = location;
		this.segments = segments;
		this.scheduledEvents = new ConcurrentHashMap<>();
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
		if (scheduledEvents.putIfAbsent(eventID, new EventSchedule(startTime, endTime)) != null) {
			throw new IllegalArgumentException("Venue is already reserved for requested date !");
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
	
	protected void cancelEvent(LocalDateTime date, int eventID) {
		if (!scheduledEvents.remove(date, eventID)) {
			throw new IllegalArgumentException("Venue is not reserved for this event at requested date !");
		}
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
	

}
