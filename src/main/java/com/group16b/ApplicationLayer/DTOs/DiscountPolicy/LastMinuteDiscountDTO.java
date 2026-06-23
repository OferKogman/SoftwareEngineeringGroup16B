package com.group16b.ApplicationLayer.DTOs.DiscountPolicy;

import java.time.LocalDateTime;

public class LastMinuteDiscountDTO extends DiscountPolicyDTO {
    private double percentage;
    private LocalDateTime lastMinuteStartDate;

    public LastMinuteDiscountDTO(double percentage, LocalDateTime lastMinuteStartDate) {
        super("Last Minute");
        this.percentage = percentage;
        this.lastMinuteStartDate = lastMinuteStartDate;
    }

    public double getPercentage() {
        return percentage;
    }

    public LocalDateTime getLastMinuteStartDate() {
        return lastMinuteStartDate;
    }
}