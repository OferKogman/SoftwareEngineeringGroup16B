package com.group16b.InfrastructureLayer;

public class RequestContext {

    private static final ThreadLocal<String> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> roleHolder = new ThreadLocal<>();

    public static void set(String userId, String role) {
        userIdHolder.set(userId);
        roleHolder.set(role);
    }

    public static String getUserId() {
        return userIdHolder.get();
    }

    public static String getRole() {
        return roleHolder.get();
    }

    public static void clear() {
        userIdHolder.remove();
        roleHolder.remove();
    }
}