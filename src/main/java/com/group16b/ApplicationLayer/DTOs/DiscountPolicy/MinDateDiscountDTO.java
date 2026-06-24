package com.group16b.ApplicationLayer.DTOs.DiscountPolicy;

import java.time.LocalDateTime;

import com.group16b.ApplicationLayer.Enums.DiscountPolicyTypes;

public class MinDateDiscountDTO extends DiscountPolicyDTO {
    private double percentage;
    private LocalDateTime startDate;

    public MinDateDiscountDTO(double percentage, LocalDateTime startDate) {
        super(DiscountPolicyTypes.MIN_DATE);
        this.percentage = percentage;
        this.startDate = startDate;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
}