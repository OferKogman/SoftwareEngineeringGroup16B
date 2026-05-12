package com.group16b.DomainLayer.Venue;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.group16b.ApplicationLayer.DTOs.ChosenSeatingSegDTO;
import com.group16b.ApplicationLayer.DTOs.EventScheduleDTO;
import com.group16b.ApplicationLayer.DTOs.FieldSegDTO;
import com.group16b.ApplicationLayer.DTOs.SegmentDTO;
import com.group16b.ApplicationLayer.DTOs.VenueDTO;

public class Venue {
	private volatile String name;
	private final Location location;
	private final Map<String, Segment> segments;
	private final Map<Integer, EventSchedule> scheduledEvents;
	private int IDForSeg = 0;

	public Venue(String name, Location location, Map<String, Segment> segments) {
		this.name = name;
		this.location = location;
		this.segments = segments;
		this.scheduledEvents = new ConcurrentHashMap<>();
	}

	public Venue(VenueDTO venueDTO){
		this.name = venueDTO.getName();
		this.location = new Location(venueDTO.getLocation());
		this.segments = new ConcurrentHashMap<>();

		for(Map.Entry<String, SegmentDTO> entry: venueDTO.getSegments().entrySet()){
            if(entry.getValue().getSegmentType().equals("S")){
                segments.put(entry.getKey(), new ChosenSeatingSeg((ChosenSeatingSegDTO)entry.getValue(), IDForSeg+""));
				IDForSeg++;
            } else if(entry.getValue().getSegmentType().equals("F")){
                segments.put(entry.getKey(), new FieldSeg((FieldSegDTO)entry.getValue(), IDForSeg+"")); 
				IDForSeg++;   
            }
        }

        this.scheduledEvents = new ConcurrentHashMap<>();
        for(Map.Entry<Integer, EventScheduleDTO> entry: venueDTO.getEventsSchedules().entrySet()){
            scheduledEvents.put(entry.getKey(), new EventSchedule(entry.getValue()));
        }
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
		if (!scheduledEvents.remove(starTime, eventID)) {
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
