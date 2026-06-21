package com.group16b.ApplicationLayer.DTOs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.group16b.DomainLayer.Venue.ChosenSeatingSeg;
import com.group16b.DomainLayer.Venue.Entrance;
import com.group16b.DomainLayer.Venue.EventSchedule;
import com.group16b.DomainLayer.Venue.FieldSeg;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Stage;
import com.group16b.DomainLayer.Venue.Venue;

public class VenueDTO {
    private String name;
    private LocationDTO location;
    private Map<String, SegmentDTO> segments;
    private Map<Integer, EventScheduleDTO> events;
    private Map<String, StageDTO> stages;
    private Map<String, EntranceDTO> entrances;
    private VenueGridDTO grid;

    public VenueDTO(Venue venue) {
        this.name = venue.getName();
        this.location = new LocationDTO(venue.getLocation());
        this.segments = new ConcurrentHashMap<>();

        for (Map.Entry<String, Segment> entry : venue.getSegments().entrySet()) {
            if (entry.getValue().getSegmentType().equals("S")) {
                segments.put(entry.getKey(), new ChosenSeatingSegDTO((ChosenSeatingSeg) entry.getValue()));
            } else if (entry.getValue().getSegmentType().equals("F")) {
                segments.put(entry.getKey(), new FieldSegDTO((FieldSeg) entry.getValue()));
            }
        }

        this.events = new ConcurrentHashMap<>();
        for (Map.Entry<Integer, EventSchedule> entry : venue.getScheduledEvents().entrySet()) {
            events.put(entry.getKey(), new EventScheduleDTO(entry.getValue()));
        }

        this.entrances = new ConcurrentHashMap<>();
        for (Map.Entry<String, Entrance> entry : venue.getEntrances().entrySet()) {
            entrances.put(entry.getKey(), new EntranceDTO(entry.getValue()));
        }

        this.stages = new ConcurrentHashMap<>();
        for (Map.Entry<String, Stage> entry : venue.getStages().entrySet()) {
            stages.put(entry.getKey(), new StageDTO(entry.getValue()));
        }

        this.grid = new VenueGridDTO(venue.getGrid());
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

    public Map<Integer, EventScheduleDTO> getEvents() {
        return events;
    }

    public void setEvents(Map<Integer, EventScheduleDTO> events) {
        this.events = events;
    }

    public Map<String, StageDTO> getStages() {
        return stages;
    }

    public void setStages(Map<String, StageDTO> stages) {
        this.stages = stages;
    }

    public Map<String, EntranceDTO> getEntrances() {
        return entrances;
    }

    public void setEntrances(Map<String, EntranceDTO> entrances) {
        this.entrances = entrances;
    }

    public VenueGridDTO getGrid() {
        return grid;
    }

    public void setGrid(VenueGridDTO grid) {
        this.grid = grid;
    }
}