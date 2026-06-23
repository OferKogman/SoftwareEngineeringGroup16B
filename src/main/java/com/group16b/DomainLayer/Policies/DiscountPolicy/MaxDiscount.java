package com.group16b.DomainLayer.Policies.DiscountPolicy;

public class MaxDiscount implements DiscountPolicy {
    private final DiscountPolicy left;
    private final DiscountPolicy right;

    public MaxDiscount(DiscountPolicy left, DiscountPolicy right) {
        if (left == null || right == null) {
            throw new IllegalArgumentException("MaxDiscount must have both left and right policies.");
        }
        this.left = left;
        this.right = right;
    }

    public DiscountPolicy getLeft() { return left; }
    public DiscountPolicy getRight() { return right; }

    @Override
    public boolean isMet(DiscountContext dc) {
        return left.isMet(dc) || right.isMet(dc);
    }

    @Override
    public double calculateDiscount(double originalPrice, DiscountContext dc) {
        double leftPrice = left.calculateDiscount(originalPrice, dc);
        double rightPrice = right.calculateDiscount(originalPrice, dc);
        return Math.min(leftPrice, rightPrice);
    }
}