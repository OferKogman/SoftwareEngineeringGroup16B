package com.group16b.ApplicationLayer.DTOs.PurchasePolicy;

import com.group16b.ApplicationLayer.Objects.PurchasePolicyTypes;

public class MinAgeDTO extends PurchasePolicyDTO {
    private int minAge;

    public MinAgeDTO(int minAge) {
        super(PurchasePolicyTypes.MIN_AGE);
        this.minAge = minAge;
    }

    public int getMinAge() {
        return minAge;
    }
}
