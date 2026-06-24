package com.group16b.ApplicationLayer.Enums;

public enum DiscountPolicyTypes {
    SIMPLE("SIMPLE"),
    AND("AND"),
    OR("OR"),
    SUM("SUM"),
    MAX("MAX"),
    COUPON("COUPON"),
    MIN_TICKETS("MIN_TICKETS"),
    MAX_TICKETS("MAX_TICKETS"),
    MIN_DATE("MIN_DATE"),
    MAX_DATE("MAX_DATE");

    private final String type;

    DiscountPolicyTypes(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}