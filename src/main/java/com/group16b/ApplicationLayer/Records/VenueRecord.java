package com.group16b.ApplicationLayer.Records;

import java.util.List;

import com.group16b.ApplicationLayer.DTOs.EventScheduleDTO;

public record VenueRecord(String name, String location, List<FieldSegRecord> fieldSeg,
        List<ChosenSeatingSegRecord> seatSeg, List<StageRecord> stages,
        List<EntranceRecord> entrances, VenueGridRecord grid, List<EventScheduleDTO> events) {
};
