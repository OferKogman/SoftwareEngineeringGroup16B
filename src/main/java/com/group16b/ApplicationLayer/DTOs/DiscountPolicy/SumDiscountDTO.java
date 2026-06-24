package com.group16b.ApplicationLayer.DTOs.DiscountPolicy;

import com.group16b.ApplicationLayer.Enums.DiscountPolicyTypes;

public class SumDiscountDTO extends DiscountPolicyDTO {
    private DiscountPolicyDTO left;
    private DiscountPolicyDTO right;

    public SumDiscountDTO(DiscountPolicyDTO left, DiscountPolicyDTO right) {
        super(DiscountPolicyTypes.SUM);
        this.left = left;
        this.right = right;
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
}
