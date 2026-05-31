package com.group16b.ApplicationLayer.DTOs;

public class ReserveFieldSeatsWithLotteryRequestDTO {
    String segmentId;
    int amount;
    String venueId;
    String lotteryCode;

    public ReserveFieldSeatsWithLotteryRequestDTO(String segmentId, int amount, String venueId, String lotteryCode) {
        this.segmentId = segmentId;
        this.amount = amount;
        this.venueId = venueId;
        this.lotteryCode = lotteryCode;
    }
    public ReserveFieldSeatsWithLotteryRequestDTO() {
    }
    public String getSegmentId() {
        return segmentId;
    }
    public int getAmount() {
        return amount;
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
    public void setAmount(int amount) {
        this.amount = amount;
    }
    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }
    public void setLotteryCode(String lotteryCode) {
        this.lotteryCode = lotteryCode;
    }
}
