package com.group16b.DomainLayer.Venue;

import java.time.LocalDateTime;

import com.group16b.ApplicationLayer.DTOs.EventScheduleDTO;

public class EventSchedule {
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