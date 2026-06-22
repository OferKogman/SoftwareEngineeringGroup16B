package com.group16b.ApplicationLayer.Objects;

public enum PurchasePolicyTypes {
    MIN_AGE("MIN_AGE"),
    MAX_AGE("MAX_AGE"),
    MIN_TICKETS("MIN_TICKETS"),
    MAX_TICKETS("MAX_TICKETS"),
    AND("AND"),
    OR("OR");

    private final String type;

    PurchasePolicyTypes(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
