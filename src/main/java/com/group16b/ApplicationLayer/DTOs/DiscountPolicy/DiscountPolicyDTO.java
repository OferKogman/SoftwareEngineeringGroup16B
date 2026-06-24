package com.group16b.ApplicationLayer.DTOs.DiscountPolicy;

import com.group16b.ApplicationLayer.Enums.DiscountPolicyTypes;

public abstract class DiscountPolicyDTO {
    protected DiscountPolicyTypes type;

    public DiscountPolicyDTO(DiscountPolicyTypes type) {
        this.type = type;
    }

    public DiscountPolicyTypes getType() {
        return type;
    }
}