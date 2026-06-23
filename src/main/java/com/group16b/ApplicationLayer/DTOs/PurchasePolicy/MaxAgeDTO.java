package com.group16b.ApplicationLayer.DTOs.PurchasePolicy;

import com.group16b.ApplicationLayer.Objects.PurchasePolicyTypes;

public class MaxAgeDTO extends PurchasePolicyDTO {
    private int maxAge;

    public MaxAgeDTO(int maxAge) {
        super(PurchasePolicyTypes.MAX_AGE);
        this.maxAge = maxAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

}
