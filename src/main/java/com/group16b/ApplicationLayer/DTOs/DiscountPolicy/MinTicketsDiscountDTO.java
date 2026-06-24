package com.group16b.ApplicationLayer.DTOs.DiscountPolicy;

import com.group16b.ApplicationLayer.Enums.DiscountPolicyTypes;

public class MinTicketsDiscountDTO extends DiscountPolicyDTO {
    private double percentage;
    private int minAmount;

    public MinTicketsDiscountDTO(double percentage, int minAmount) {
        super(DiscountPolicyTypes.MIN_TICKETS);
        this.percentage = percentage;
        this.minAmount = minAmount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(int minAmount) {
        this.minAmount = minAmount;
    }
}