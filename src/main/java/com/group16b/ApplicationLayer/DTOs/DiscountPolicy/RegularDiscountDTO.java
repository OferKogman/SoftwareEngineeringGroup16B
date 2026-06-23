package com.group16b.ApplicationLayer.DTOs.DiscountPolicy;

public class RegularDiscountDTO extends DiscountPolicyDTO {
    private double percentage;

    public RegularDiscountDTO(double percentage) {
        super("Regular");
        this.percentage = percentage;
    }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
}