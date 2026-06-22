package com.group16b.DomainLayer.Policies.DiscountPolicy;

import java.util.List;

public class OrDiscount implements DiscountPolicy {
    private final List<DiscountPolicy> children;
    private final double discountPercentage;

    public OrDiscount(List<DiscountPolicy> children, double discountPercentage) {
        if (children == null || children.isEmpty())
            throw new IllegalArgumentException("OrDiscount must have at least one child.");
        if (discountPercentage < 0 || discountPercentage > 100)
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        this.children = children;
        this.discountPercentage = discountPercentage;
    }

    public List<DiscountPolicy> getChildren() { return children; }
    public double getDiscountPercentage() { return discountPercentage; }

    @Override
    public boolean isMet(DiscountContext context) {
        return children.stream().anyMatch(child -> child.isMet(context));
    }

    @Override
    public double calculateDiscount(double originalPrice, DiscountContext context) {
        return isMet(context) ? originalPrice * (1 - discountPercentage / 100) : originalPrice;
    }
}