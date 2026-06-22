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

    public boolean isMet(DiscountContext dc){
        for (DiscountPolicy policy : policies) {
            if(policy.isMet(dc)) return true;
        }
        return false;
    }
    @Override
    public double calculateDiscount(double originalPrice, DiscountContext dc) {
        double price = originalPrice;
        DiscountContext context = dc;
        for (DiscountPolicy policy : policies) {
            price = Math.max(policy.calculateDiscount(price,context), 0);
        }
        return Math.max(price,0);
    }
}