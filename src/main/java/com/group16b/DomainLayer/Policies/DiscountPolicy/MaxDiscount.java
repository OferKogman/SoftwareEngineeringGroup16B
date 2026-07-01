package com.group16b.DomainLayer.Policies.DiscountPolicy;

import java.util.ArrayList;
import java.util.List;

public class MaxDiscount implements DiscountPolicy {
    private DiscountPolicy left;
    private DiscountPolicy right;

    public MaxDiscount() {
    }

    public MaxDiscount(DiscountPolicy left, DiscountPolicy right) {
        if (left == null || right == null) {
            throw new IllegalArgumentException("MaxDiscount must have both left and right policies.");
        }

        this.left = left;
        this.right = right;
    }

    public MaxDiscount(List<DiscountPolicy> policies) {
        if (policies == null || policies.isEmpty()) {
            throw new IllegalArgumentException("MaxDiscount must have at least one policy.");
        }

        this.left = policies.get(0);

        if (policies.size() == 2) {
            this.right = policies.get(1);
        } else if (policies.size() > 2) {
            this.right = new MaxDiscount(policies.subList(1, policies.size()));
        }
    }

    public DiscountPolicy getLeft() {
        return left;
    }

    public void setLeft(DiscountPolicy left) {
        this.left = left;
    }

    public DiscountPolicy getRight() {
        return right;
    }

    public void setRight(DiscountPolicy right) {
        this.right = right;
    }

    public List<DiscountPolicy> getPolicies() {
        List<DiscountPolicy> result = new ArrayList<>();

        if (left != null) {
            result.add(left);
        }

        if (right instanceof MaxDiscount maxDiscount) {
            result.addAll(maxDiscount.getPolicies());
        } else if (right != null) {
            result.add(right);
        }

        return result;
    }

    @Override
    public boolean isMet(DiscountContext context) {
        return (left != null && left.isMet(context))
                || (right != null && right.isMet(context));
    }

    @Override
    public double calculateDiscount(double originalPrice, DiscountContext context) {
        double bestPrice = originalPrice;

        if (left != null) {
            bestPrice = Math.min(bestPrice, left.calculateDiscount(originalPrice, context));
        }

        if (right != null) {
            bestPrice = Math.min(bestPrice, right.calculateDiscount(originalPrice, context));
        }

        return bestPrice;
    }
}