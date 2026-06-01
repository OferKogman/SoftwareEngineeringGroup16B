package com.group16b.ApplicationLayer.DTOs;

public class AdminLoginRequestDTO {
    private String email; 
    private String password;
    private String id;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getID() { return id; } 
    public void setID(String id) { this.id = id;}
}
