package com.group16b.ApplicationLayer.DTOs;



import com.group16b.DomainLayer.User.User;


public class UserDTO {
    private String userEmail;
	private long version;

    public UserDTO(User user) {
        this.userEmail = user.getEmail();
        this.version = user.getVersion();
    }

}
