package com.group16b.ApplicationLayer.DTOs.DiscountPolicy;

public class MinimumPurchaseDiscountDTO extends DiscountPolicyDTO {
    private double percentage;
    private int minimumAmount;

    public MinimumPurchaseDiscountDTO(double percentage, int minimumAmount) {
        super("Minimum Purchase");
        this.percentage = percentage;
        this.minimumAmount = minimumAmount;
    }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
    public int getMinimumAmount() { return minimumAmount; }
    public void setMinimumAmount(int minimumAmount) { this.minimumAmount = minimumAmount; }
}