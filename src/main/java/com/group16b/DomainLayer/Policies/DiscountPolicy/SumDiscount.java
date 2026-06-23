package com.group16b.DomainLayer.Policies.DiscountPolicy;

public class SumDiscount implements DiscountPolicy {
    private final DiscountPolicy left;
    private final DiscountPolicy right;

    public SumDiscount(DiscountPolicy left, DiscountPolicy right) {
        if (left == null || right == null)
            throw new IllegalArgumentException("SumDiscount must have both left and right children.");
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
        double after = originalPrice;
        after = left.calculateDiscount(after, context);
        after = right.calculateDiscount(after, context);
        return Math.max(after, 0);
    }
}