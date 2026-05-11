package com.group16b.DomainLayer.Policies;

public interface DiscountPolicy {
    /**
     * Calculates the discount amount based on the original price.
     *
     * @param originalPrice The original price of the item or service.
     * @return The discount amount to be applied.
     */
    double calculateDiscount(double originalPrice);
}
