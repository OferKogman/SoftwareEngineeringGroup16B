package com.group16b.ApplicationLayer.DTOs.DiscountPolicy;

public class MinimumPurchaseDiscountDTO extends DiscountPolicyDTO {
    private double percentage;
    private Integer minimumAmount;

    public MinimumPurchaseDiscountDTO(double percentage, Integer minimumAmount) {
        super("Minimum Purchase");
        this.percentage = percentage;
        this.minimumAmount = minimumAmount;
    }

    public double getPercentage() {
        return percentage;
    }

    public Integer getMinimumAmount() {
        return minimumAmount;
    }
}