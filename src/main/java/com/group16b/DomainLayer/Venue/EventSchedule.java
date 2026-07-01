package com.group16b.DomainLayer.Venue;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.group16b.ApplicationLayer.DTOs.EventScheduleDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table (name = "event_schedules")
public class EventSchedule {
    
    @Id
    @Column(name = "db_id")
    private String dbId;

    @Column(name = "event_id")
    private Integer eventId; // Required to map correctly in Venue

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public EventSchedule(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) throw new IllegalArgumentException("Start and end times cannot be null.");
        if (!startTime.isBefore(endTime)) throw new IllegalArgumentException("Event start time must be before end time!");
        
        this.dbId = UUID.randomUUID().toString();
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public EventSchedule() {
        this.dbId = UUID.randomUUID().toString();
    }

    public EventSchedule(EventScheduleDTO eventScheduleDTO){
        this.dbId = UUID.randomUUID().toString();
        this.startTime = eventScheduleDTO.getStartTime();
        this.endTime = eventScheduleDTO.getEndTime();    
    }

    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId = eventId; }

    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }

    public boolean overlapsWith(EventSchedule other) {
        return this.startTime.isBefore(other.endTime) && this.endTime.isAfter(other.startTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventSchedule other)) return false;
        return dbId != null && dbId.equals(other.dbId);
    }

    @Override
    public int hashCode() { return Objects.hash(dbId); }
}