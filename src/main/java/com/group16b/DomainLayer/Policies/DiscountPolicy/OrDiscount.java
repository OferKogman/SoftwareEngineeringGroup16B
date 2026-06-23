package com.group16b.DomainLayer.Policies.DiscountPolicy;

public class OrDiscount implements DiscountPolicy {
    private final DiscountPolicy left;
    private final DiscountPolicy right;
    private final double discountPercentage;

    public OrDiscount(DiscountPolicy left, DiscountPolicy right, double discountPercentage) {
        if (left == null || right == null)
            throw new IllegalArgumentException("OrDiscount must have both left and right children.");
        if (discountPercentage < 0 || discountPercentage > 100)
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        this.left = left;
        this.right = right;
        this.discountPercentage = discountPercentage;
    }

    public DiscountPolicy getLeft() { return left; }
    public DiscountPolicy getRight() { return right; }
    public double getDiscountPercentage() { return discountPercentage; }

    @Override
    public boolean isMet(DiscountContext context) {
        return left.isMet(context) || right.isMet(context);
    }

    @Override
    public double calculateDiscount(double originalPrice, DiscountContext context) {
        return isMet(context) ? originalPrice * (1 - discountPercentage / 100) : originalPrice;
    }
}