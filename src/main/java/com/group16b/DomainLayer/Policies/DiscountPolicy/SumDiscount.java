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
        double totalDiscount = 0;
        for (DiscountPolicy policy : policies) {
            totalDiscount += originalPrice - policy.calculateDiscount(originalPrice);
        }
        double finalPrice = originalPrice - totalDiscount;
        return Math.max(0, finalPrice);
    }
}