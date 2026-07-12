package com.group16b.DomainLayer.Policies.DiscountPolicy;

import java.util.ArrayList;
import java.util.List;

public class SumDiscount implements DiscountPolicy {
    private DiscountPolicy left;
    private DiscountPolicy right;

    public SumDiscount() {
    }

    public SumDiscount(DiscountPolicy left, DiscountPolicy right) {
        if (left == null || right == null) {
            throw new IllegalArgumentException("SumDiscount must have both left and right policies.");
        }

        this.left = left;
        this.right = right;
    }

    public SumDiscount(List<DiscountPolicy> policies) {
        if (policies == null || policies.isEmpty()) {
            throw new IllegalArgumentException("SumDiscount must have at least one policy.");
        }

        this.left = policies.get(0);

        if (policies.size() == 2) {
            this.right = policies.get(1);
        } else if (policies.size() > 2) {
            this.right = new SumDiscount(policies.subList(1, policies.size()));
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

        if (right instanceof SumDiscount sumDiscount) {
            result.addAll(sumDiscount.getPolicies());
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
        double price = originalPrice;

        if (left != null) {
            price = left.calculateDiscount(price, context);
        }

        if (right != null) {
            price = right.calculateDiscount(price, context);
        }

        return Math.max(price, 0);
    }
}