package com.group16b.ApplicationLayer.DTOs.DiscountPolicy;

public abstract class DiscountPolicyDTO {
    private String type;

    public DiscountPolicyDTO(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}