package com.group16b.ApplicationLayer.DTOs;

import java.util.List;

public class ReserveSeatsWithLotteryRequestDTO {
    String segmentId;
    List<String> seatIds;
    String venueId;
    String lotteryCode;

    public ReserveSeatsWithLotteryRequestDTO(String segmentId, List<String> seatIds, String venueId, String lotteryCode) {
        this.segmentId = segmentId;
        this.seatIds = seatIds;
        this.venueId = venueId;
        this.lotteryCode = lotteryCode;
    }
    public ReserveSeatsWithLotteryRequestDTO() {
    }
    public String getSegmentId() {
        return segmentId;
    }
    public List<String> getSeatIds() {
        return seatIds;
    }
    public String getVenueId() {
        return venueId;
    }
    public String getLotteryCode() {
        return lotteryCode;
    }

    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }
    public void setSeatIds(List<String> seatIds) {
        this.seatIds = seatIds;
    }
    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }
    public void setLotteryCode(String lotteryCode) {
        this.lotteryCode = lotteryCode;
    }
}
