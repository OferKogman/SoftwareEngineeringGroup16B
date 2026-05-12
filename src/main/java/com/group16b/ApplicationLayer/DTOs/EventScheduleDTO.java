package com.group16b.ApplicationLayer.DTOs;

import java.time.LocalDateTime;

import com.group16b.DomainLayer.Venue.EventSchedule;

public class EventScheduleDTO {
    private LocalDateTime startTime;
    private LocalDateTime endTime;


    public EventScheduleDTO(EventSchedule eventSchedule) {
        this.startTime = eventSchedule.getStartTime();
        this.endTime = eventSchedule.getEndTime();
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}