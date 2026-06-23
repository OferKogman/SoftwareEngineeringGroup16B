package com.group16b.DomainLayer.Policies.DiscountPolicy;

public class SumDiscount implements DiscountPolicy {
    private final DiscountPolicy left;
    private final DiscountPolicy right;

    public SumDiscount(DiscountPolicy left, DiscountPolicy right) {
        if (left == null || right == null) {
            throw new IllegalArgumentException("SumDiscount must have both left and right policies.");
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
        double price = originalPrice;
        price = left.calculateDiscount(price, dc);
        price = right.calculateDiscount(price, dc);
        return Math.max(price, 0);
    }
}