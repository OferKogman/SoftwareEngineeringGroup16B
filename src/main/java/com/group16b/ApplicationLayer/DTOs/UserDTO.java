package com.group16b.ApplicationLayer.DTOs;



import com.group16b.DomainLayer.User.User;


public class UserDTO {
    private final int userID;
	private String email;

    public UserDTO(User user) {
        this.userID = user.getUserID();
        this.email = user.getEmail();
    }

}
