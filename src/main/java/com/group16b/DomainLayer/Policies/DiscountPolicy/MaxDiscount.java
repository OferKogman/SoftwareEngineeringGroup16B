package com.group16b.DomainLayer.Policies.DiscountPolicy;

import java.util.List;

public class MaxDiscount implements DiscountPolicy {
    private List<DiscountPolicy> policies;

    public MaxDiscount(List<DiscountPolicy> policies) {
        if (policies == null || policies.isEmpty()) {
            throw new IllegalArgumentException("MaxDiscount must have at least one policy.");
        }
        this.policies = policies;
    }

    public List<DiscountPolicy> getPolicies() { return policies; }

    public boolean isMet(DiscountContext dc){
        for (DiscountPolicy policy : policies) {
            if(policy.isMet(dc)) return true;
        }
        return false;
    }
    @Override
    public double calculateDiscount(double originalPrice, DiscountContext dc) {
        double bestPrice = originalPrice;
        DiscountContext context = dc;
        for (DiscountPolicy policy : policies) {
            double discountedPrice = policy.calculateDiscount(originalPrice, context);
            if (discountedPrice < bestPrice) {
                bestPrice = discountedPrice;
            }
        }
        return bestPrice;
    }
}