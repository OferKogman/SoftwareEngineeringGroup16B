package com.group16b.InfrastructureLayer.Security;

public enum Role {
    SIGNED("Signed"),
    ADMIN("Admin"),
    GUEST("Guest");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String value() {
        return displayName;
    }
}
