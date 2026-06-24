package com.group16b.ApplicationLayer.DTOs.DiscountPolicy;

import com.group16b.ApplicationLayer.Enums.DiscountPolicyTypes;

public class SimpleDiscountDTO extends DiscountPolicyDTO {
    private double percentage;

    public SimpleDiscountDTO(double percentage) {
        super(DiscountPolicyTypes.SIMPLE);
        this.percentage = percentage;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}