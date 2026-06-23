package com.group16b.ApplicationLayer.DTOs.DiscountPolicy;

import java.time.LocalDateTime;

public class EarlyBirdDiscountDTO extends DiscountPolicyDTO {
    private double percentage;
    private LocalDateTime earlyBirdEndDate;

    public EarlyBirdDiscountDTO(double percentage, LocalDateTime earlyBirdEndDate) {
        super("Early Bird");
        this.percentage = percentage;
        this.earlyBirdEndDate = earlyBirdEndDate;
    }

    public double getPercentage() {
        return percentage;
    }

    public LocalDateTime getEarlyBirdEndDate() {
        return earlyBirdEndDate;
    }
}