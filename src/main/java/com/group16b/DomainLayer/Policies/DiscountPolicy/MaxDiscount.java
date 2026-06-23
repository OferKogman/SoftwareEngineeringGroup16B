package com.group16b.DomainLayer.Policies.DiscountPolicy;

public class MaxDiscount implements DiscountPolicy {
    private final DiscountPolicy left;
    private final DiscountPolicy right;

    public MaxDiscount(DiscountPolicy left, DiscountPolicy right) {
        if (left == null || right == null)
            throw new IllegalArgumentException("MaxDiscount must have both left and right children.");
        this.left = left;
        this.right = right;
    }

    public DiscountPolicy getLeft() { return left; }
    public DiscountPolicy getRight() { return right; }

    @Override
    public boolean isMet(DiscountContext context) {
        return left.isMet(context) || right.isMet(context);
    }

    @Override
    public double calculateDiscount(double originalPrice, DiscountContext context) {
        double leftPrice = left.calculateDiscount(originalPrice, context);
        double rightPrice = right.calculateDiscount(originalPrice, context);
        return Math.min(leftPrice, rightPrice);
    }
}