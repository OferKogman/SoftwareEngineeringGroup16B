package com.group16b.DomainLayer.SystemAdmin;

public class SystemAdmin {
    private int id;
    private String username;
    private String passwordHash;

    public SystemAdmin(int id, String username, String passwordHash) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }


}
