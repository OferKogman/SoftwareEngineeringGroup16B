package com.group16b.ApplicationLayer.DTOs.DiscountPolicy;

import com.group16b.ApplicationLayer.Enums.DiscountPolicyTypes;

public class MaxTicketsDiscountDTO extends DiscountPolicyDTO {
    private double percentage;
    private int maxAmount;

    public MaxTicketsDiscountDTO(double percentage, int maxAmount) {
        super(DiscountPolicyTypes.MAX_TICKETS);
        this.percentage = percentage;
        this.maxAmount = maxAmount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(int maxAmount) {
        this.maxAmount = maxAmount;
    }
}