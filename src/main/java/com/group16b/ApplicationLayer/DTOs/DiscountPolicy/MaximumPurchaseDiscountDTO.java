package com.group16b.ApplicationLayer.DTOs.DiscountPolicy;

public class MaximumPurchaseDiscountDTO extends DiscountPolicyDTO {
    private double percentage;
    private Integer maximumAmount;

    public MaximumPurchaseDiscountDTO(double percentage, Integer maximumAmount) {
        super("Maximum Purchase");
        this.percentage = percentage;
        this.maximumAmount = maximumAmount;
    }

    public double getPercentage() {
        return percentage;
    }

    public Integer getMaximumAmount() {
        return maximumAmount;
    }
}