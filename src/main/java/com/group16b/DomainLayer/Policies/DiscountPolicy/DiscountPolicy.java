package com.group16b.DomainLayer.Policies.DiscountPolicy;

import java.time.LocalDateTime;

public interface DiscountPolicy {
    boolean isMet(DiscountContext context);

    double calculateDiscount(double originalPrice, DiscountContext context);

    default double calculateDiscount(double originalPrice) {
        return calculateDiscount(originalPrice, new DiscountContext(0, 0, LocalDateTime.now(), null));
    }
}