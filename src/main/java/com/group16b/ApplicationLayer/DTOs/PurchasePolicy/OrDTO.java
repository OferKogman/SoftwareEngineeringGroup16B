package com.group16b.ApplicationLayer.DTOs.PurchasePolicy;

import com.group16b.ApplicationLayer.Enums.PurchasePolicyTypes;

public class OrDTO extends PurchasePolicyDTO {
    private PurchasePolicyDTO left;
    private PurchasePolicyDTO right;

    public OrDTO(PurchasePolicyDTO left, PurchasePolicyDTO right) {
        super(PurchasePolicyTypes.OR);
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
