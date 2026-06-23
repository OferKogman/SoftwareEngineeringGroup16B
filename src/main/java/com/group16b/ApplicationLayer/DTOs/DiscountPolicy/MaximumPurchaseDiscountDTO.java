package com.group16b.ApplicationLayer.DTOs.DiscountPolicy;

public class MaximumPurchaseDiscountDTO extends DiscountPolicyDTO {
    private double percentage;
    private int maximumAmount;

    public MaximumPurchaseDiscountDTO(double percentage, int maximumAmount) {
        super("Maximum Purchase");
        this.percentage = percentage;
        this.maximumAmount = maximumAmount;
    }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
    public int getMaximumAmount() { return maximumAmount; }
    public void setMaximumAmount(int maximumAmount) { this.maximumAmount = maximumAmount; }
}