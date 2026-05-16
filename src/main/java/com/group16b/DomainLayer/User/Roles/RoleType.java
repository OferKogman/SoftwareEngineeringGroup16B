package com.group16b.DomainLayer.User.Roles;

public enum RoleType {
    OWNER,
    MANAGER,
    FOUNDER;
    
    public boolean isLowerOrEqual(RoleType other) {
        return this.level() <= other.level();
    }

    public boolean isHigherOrEqual(RoleType other) {
        return this.level() >= other.level();
    }

    private int level() {
        return switch (this) {
            case MANAGER -> 1;
            case OWNER -> 2;
            case FOUNDER -> 3;
        };
    }
}
