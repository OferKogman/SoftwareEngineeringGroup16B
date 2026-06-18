package com.group16b.ApplicationLayer.DTOs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.group16b.ApplicationLayer.Records.EntranceRecord;
import com.group16b.ApplicationLayer.Records.GridRectangleRecord;
import com.group16b.ApplicationLayer.Records.StageRecord;
import com.group16b.ApplicationLayer.Records.VenueGridRecord;
import com.group16b.DomainLayer.Venue.ChosenSeatingSeg;
import com.group16b.DomainLayer.Venue.Entrance;
import com.group16b.DomainLayer.Venue.EventSchedule;
import com.group16b.DomainLayer.Venue.FieldSeg;
import com.group16b.DomainLayer.Venue.GridRectangle;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Stage;
import com.group16b.DomainLayer.Venue.Venue;

public class VenueDTO {
    private String name;
    private LocationDTO location;
    private Map<String, SegmentDTO> segments;
    private Map<Integer, EventScheduleDTO> events;
    private Map<String, StageRecord> stages;
    private Map<String, EntranceRecord> entrances;
    private VenueGridRecord grid;

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
            GridRectangle currArea = entry.getValue().getArea();
            entrances.put(entry.getKey(),
                    new EntranceRecord(entry.getKey(), new GridRectangleRecord(currArea.getStartRow(),
                            currArea.getStartColumn(), currArea.getRowCount(), currArea.getColumnCount())));
        }

        this.stages = new ConcurrentHashMap<>();
        for (Map.Entry<String, Stage> entry : venue.getStages().entrySet()) {
            GridRectangle currArea = entry.getValue().getArea();
            stages.put(entry.getKey(),
                    new StageRecord(entry.getKey(), new GridRectangleRecord(currArea.getStartRow(),
                            currArea.getStartColumn(), currArea.getRowCount(), currArea.getColumnCount())));
        }

        this.grid = new VenueGridRecord(venue.getGrid().getRows(), venue.getGrid().getColumns());
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

    public VenueGridRecord getGrid() {
        return grid;
    }

    public void setGrid(VenueGridRecord grid) {
        this.grid = grid;
    }
}