package com.group16b.DomainLayer.User;

import java.util.HashMap;
import java.util.Map;

public class IUserRepository{
    
    private static IUserRepository instance;
    private Map<Integer, User> users = new HashMap<>();
    
    private IUserRepository() {
    }
    
    public static synchronized IUserRepository getInstance() {
        if (instance == null) {
            instance = new IUserRepository();
        }
        return instance;
    }
    
    public User getUserByID(int userID) {
        return users.get(userID);
    }
    
    public void addUser(User user) {
        users.put(user.getUserID(), user);
    }
    
    public void updateUser(User user) {
        users.put(user.getUserID(), user);
    }
    
    public void deleteUser(int userID) {
        users.remove(userID);
    }
    
    public boolean userExists(int userID) {
        return users.containsKey(userID);
    }
}