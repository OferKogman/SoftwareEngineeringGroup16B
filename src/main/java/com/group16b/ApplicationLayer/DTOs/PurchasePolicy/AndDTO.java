package com.group16b.ApplicationLayer.DTOs.PurchasePolicy;

import com.group16b.ApplicationLayer.Objects.PurchasePolicyTypes;

public class AndDTO extends PurchasePolicyDTO {
    private PurchasePolicyDTO left;
    private PurchasePolicyDTO right;

    public AndDTO(PurchasePolicyDTO left, PurchasePolicyDTO right) {
        super(PurchasePolicyTypes.AND);
        this.left = left;
        this.right = right;
    }

    public PurchasePolicyDTO getLeft() {
        return left;
    }

    public PurchasePolicyDTO getRight() {
        return right;
    }
}
