package com.group16b.DomainLayer.Policies.DiscountPolicy;

public class SimpleDiscount implements DiscountPolicy{
    private double discountPercentage;
    public SimpleDiscount(double discountPercentage) {
        if(discountPercentage<0 || discountPercentage >100){
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        }
        this.discountPercentage = discountPercentage;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public double calculateDiscount(double originalPrice){
        return originalPrice * (1-this.discountPercentage/100);
    }
}
