package com.group16b.DomainLayer.Policies.DiscountPolicy;

public abstract class ConditionalDiscount extends Discount{
    public ConditionalDiscount(double discountPercentage, double discountAmount) {
        super(discountPercentage, discountAmount);
    }
}
