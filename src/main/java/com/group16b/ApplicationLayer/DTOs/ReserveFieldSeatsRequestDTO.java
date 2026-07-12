package com.group16b.ApplicationLayer.DTOs;

public class ReserveFieldSeatsRequestDTO {
    String segmentId;
    int amount;
    String venueId;
    int age;

    public ReserveFieldSeatsRequestDTO(String segmentId, int amount, String venueId) {
        this.segmentId = segmentId;
        this.amount = amount;
        this.venueId = venueId;
    }
    public ReserveFieldSeatsRequestDTO() {
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
    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }
    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}