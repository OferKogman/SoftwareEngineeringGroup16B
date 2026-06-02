package com.group16b.DomainLayer.Policies.DiscountPolicy;

import java.util.List;

public class SumDiscount implements DiscountPolicy {
    private List<DiscountPolicy> policies;

    public SumDiscount(List<DiscountPolicy> policies) {
        if (policies == null || policies.isEmpty()) {
            throw new IllegalArgumentException("SumDiscount must have at least one policy.");
        }
        this.policies = policies;
    }

    public List<DiscountPolicy> getPolicies() { return policies; }

    @Override
    public double calculateDiscount(double originalPrice) {
        double price = originalPrice;
        for (DiscountPolicy policy : policies) {
            price = Math.max(policy.calculateDiscount(price), 0);
        }
        return Math.max(price,0);
    }
}