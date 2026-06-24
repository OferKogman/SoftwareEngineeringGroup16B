package com.group16b.ApplicationLayer.DTOs.DiscountPolicy;

import java.time.LocalDateTime;

import com.group16b.ApplicationLayer.Enums.DiscountPolicyTypes;

public class MaxDateDiscountDTO extends DiscountPolicyDTO {
    private double percentage;
    private LocalDateTime endDate;

    public MaxDateDiscountDTO(double percentage, LocalDateTime endDate) {
        super(DiscountPolicyTypes.MAX_DATE);
        this.percentage = percentage;
        this.endDate = endDate;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
}