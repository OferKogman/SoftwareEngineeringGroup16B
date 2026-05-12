package com.group16b.ApplicationLayer.DTOs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.group16b.DomainLayer.Venue.ChosenSeatingSeg;
import com.group16b.DomainLayer.Venue.EventSchedule;
import com.group16b.DomainLayer.Venue.FieldSeg;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Venue;

public class VenueDTO {
    private String name;
    private LocationDTO location; 
    private Map<String, SegmentDTO> segments;
    private Map<Integer, EventScheduleDTO> events;

    public VenueDTO(Venue venue) {
        this.name = venue.getName();
        this.location = new LocationDTO(venue.getLocation());
        this.segments = new ConcurrentHashMap<>();

        for(Map.Entry<String, Segment> entry: venue.getSegments().entrySet()){
            if(entry.getValue().getSegmentType().equals("S")){
                segments.put(entry.getKey(), new ChosenSeatingSegDTO((ChosenSeatingSeg)entry.getValue()));
            } else if(entry.getValue().getSegmentType().equals("F")){
                segments.put(entry.getKey(), new FieldSegDTO((FieldSeg)entry.getValue()));    
            }
        }

        this.events = new ConcurrentHashMap<>();
        for(Map.Entry<Integer, EventSchedule> entry: venue.getScheduledEvents().entrySet()){
            events.put(entry.getKey(), new EventScheduleDTO(entry.getValue()));
        }

    }

    public String getName() { 
        return name; 
    }

    public void setName(String name) { 
        this.name = name; 
    }

    public LocationDTO getLocation() { 
        return location; 
    }

    public void setLocation(LocationDTO location) { 
        this.location = location; 
    }

    public Map<String, SegmentDTO> getSegments() { 
        return segments; 
    }

    public Map<Integer, EventScheduleDTO> getEventsSchedules() { 
        return events; 
    }

    public void setSegments(Map<String, SegmentDTO> segments) { 
        this.segments = segments; 
    }
}