package com.group16b.ApplicationLayer;

import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.User;

public class UserService {
    
    private IUserRepository userRepository;
    
    public UserService() {
        this.userRepository = IUserRepository.getInstance();
    }
    
    public User getUserByID(int userID) {
        return userRepository.getUserByID(userID);
    }
    
    public void registerUser(String email, String password) {
        User newUser = new User(email, password);
        userRepository.addUser(newUser);
    }
    
    public void updateUserPassword(int userID, String oldPassword, String newPassword) {
        User user = userRepository.getUserByID(userID);
        if(user == null) {
            System.out.println("User not found.");
            return;
        }
        if(!user.confirmPassword(oldPassword)) {
            System.out.println("Old password is incorrect.");
            return;
        }
        if(!user.confirmPassword(newPassword)) {
            System.out.println("New password cannot be the same as the old password.");
            return;
        }//else, user is not null and old password is correct and new password is different from old password
        user.setPassword(newPassword);
        userRepository.updateUser(user);
        
    }
    
    public boolean authenticateUser(int userID, String password) {
        User user = userRepository.getUserByID(userID);
        if (user != null) {
            return user.confirmPassword(password);
        }
        return false;
    }
    
    public void deleteUser(int userID) {
        userRepository.deleteUser(userID);
    }
    
    public boolean userExists(int userID) {
        return userRepository.userExists(userID);
    }
}