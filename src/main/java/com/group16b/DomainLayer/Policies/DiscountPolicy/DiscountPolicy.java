package com.group16b.DomainLayer.Policies.DiscountPolicy;

public interface DiscountPolicy {
    double calculateDiscount(double originalPrice, DiscountContext context);
    boolean isMet(DiscountContext context);
}
