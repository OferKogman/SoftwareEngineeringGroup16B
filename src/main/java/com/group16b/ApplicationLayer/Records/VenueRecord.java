package com.group16b.ApplicationLayer.Records;

import java.util.List;
import java.util.Map;

import com.group16b.ApplicationLayer.DTOs.EventScheduleDTO;
import com.group16b.ApplicationLayer.DTOs.LocationDTO;
import com.group16b.ApplicationLayer.DTOs.SegmentDTO;

public record VenueRecord(String name, String location, List<FieldSegRecord> fieldSeg, List<ChosenSeatingSegRecord> seatSeg, List<StageRecord> stages,
    List<EntranceRecord> entrances, VenueGridRecord grid, List<EventScheduleDTO> events) {};
