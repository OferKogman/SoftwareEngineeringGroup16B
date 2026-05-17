package com.group16b.ApplicationLayer.DTOs;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.User.Roles.Founder;
import com.group16b.DomainLayer.User.Roles.Owner;
import com.group16b.DomainLayer.User.Roles.Manager;
import com.group16b.DomainLayer.User.Roles.Member;
import com.group16b.DomainLayer.User.Roles.Role;

public class UserDTO {
    private final int userID;
	private String email;

    public UserDTO(User user) {
        this.userID = user.getUserID();
        this.email = user.getEmail();
    }

}
