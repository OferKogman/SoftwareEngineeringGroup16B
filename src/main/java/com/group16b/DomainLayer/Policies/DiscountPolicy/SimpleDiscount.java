package com.group16b.DomainLayer.Policies.DiscountPolicy;

public class SimpleDiscount implements DiscountPolicy{
    private double discountPercentage;
    public SimpleDiscount(double discountPercentage) {
        if(discountPercentage<0 || discountPercentage >100){
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        }
        this.discountPercentage = discountPercentage;
    }


    public boolean isMet(DiscountContext dc){
        return true; //discount is NOT conditional, unnecessary to check.
    }

    public double calculateDiscount(double originalPrice, DiscountContext dc){
        return originalPrice * (1-this.discountPercentage/100);
    }
}
