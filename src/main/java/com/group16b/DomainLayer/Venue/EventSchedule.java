package com.group16b.DomainLayer.Venue;

import java.time.LocalDateTime;

import com.group16b.ApplicationLayer.DTOs.EventScheduleDTO;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
@Entity
@Table (name = "event_schedules")
public class EventSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer dbId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public EventSchedule(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start and end times cannot be null.");
        }
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Event start time must be before end time!");
        }
        
        this.startTime = startTime;
        this.endTime = endTime;
    }
    public EventSchedule() {
        // Default constructor for JPA
        this.startTime = null;
        this.endTime = null;
    }

    public EventSchedule(EventScheduleDTO eventScheduleDTO){
        this.startTime = eventScheduleDTO.getStartTime();
        this.endTime = eventScheduleDTO.getEndTime();    
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public boolean overlapsWith(EventSchedule other) {
        return this.startTime.isBefore(other.endTime) && this.endTime.isAfter(other.startTime);
    }
}