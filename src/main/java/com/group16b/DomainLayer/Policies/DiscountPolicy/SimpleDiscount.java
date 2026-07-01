package com.group16b.DomainLayer.Policies.DiscountPolicy;

public class SimpleDiscount implements DiscountPolicy {
    private double discountPercentage;

    public SimpleDiscount() {
    }

    public SimpleDiscount(double discountPercentage) {
        if (discountPercentage < 0 || discountPercentage > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        }
        this.discountPercentage = discountPercentage;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        if (discountPercentage < 0 || discountPercentage > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        }
        this.discountPercentage = discountPercentage;
    }

    @Override
    public boolean isMet(DiscountContext context) {
        return true;
    }

    @Override
    public double calculateDiscount(double originalPrice, DiscountContext context) {
        return originalPrice * (1 - discountPercentage / 100);
    }
}