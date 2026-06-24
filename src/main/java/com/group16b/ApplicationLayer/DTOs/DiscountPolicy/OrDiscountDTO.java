package com.group16b.ApplicationLayer.DTOs.DiscountPolicy;

import com.group16b.ApplicationLayer.Enums.DiscountPolicyTypes;

public class OrDiscountDTO extends DiscountPolicyDTO {
    private DiscountPolicyDTO left;
    private DiscountPolicyDTO right;
    private double percentage;

    public OrDiscountDTO(DiscountPolicyDTO left, DiscountPolicyDTO right, double percentage) {
        super(DiscountPolicyTypes.OR);
        this.left = left;
        this.right = right;
        this.percentage = percentage;
    }

    public DiscountPolicyDTO getLeft() {
        return left;
    }

    public void setLeft(DiscountPolicyDTO left) {
        this.left = left;
    }

    public DiscountPolicyDTO getRight() {
        return right;
    }

    public void setRight(DiscountPolicyDTO right) {
        this.right = right;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
