package com.group16b.ApplicationLayer.DTOs;



import com.group16b.DomainLayer.User.User;


public class UserDTO {
	private String email;

    public UserDTO(User user) {
        this.email = user.getEmail();
    }

}
