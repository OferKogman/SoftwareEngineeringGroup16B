package com.group16b.DomainLayer.User;

import java.util.HashMap;
import com.group16b.DomainLayer.User.Roles.Role; 

public class User {
    
    private String email;
    private String password;
    private HashMap<Integer, Role> roles; // Key: companyID, Value: Role
    
    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.roles = new HashMap<>();
    }
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
    public Role getRole(int companyID) {
        return roles.get(companyID);
    }
}
