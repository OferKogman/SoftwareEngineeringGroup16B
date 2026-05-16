package com.group16b.ApplicationLayer.DTOs;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.group16b.ApplicationLayer.DTOs.Roles.RoleDTO;
import com.group16b.ApplicationLayer.DTOs.Roles.FounderDTO;
import com.group16b.ApplicationLayer.DTOs.Roles.OwnerDTO;
import com.group16b.ApplicationLayer.DTOs.Roles.ManagerDTO;
import com.group16b.ApplicationLayer.DTOs.Roles.MemberDTO;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.User.Roles.Founder;
import com.group16b.DomainLayer.User.Roles.Owner;
import com.group16b.DomainLayer.User.Roles.Manager;
import com.group16b.DomainLayer.User.Roles.Member;
import com.group16b.DomainLayer.User.Roles.Role;

public class UserDTO {
    private final int userID;
	private String email;
	private HashMap<Integer, RoleDTO> roles;

    public UserDTO(User user) {
        this.userID = user.getEmail();
        this.email = user.getEmail();
        this.roles = user.getRoles().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> toRoleDTO(entry.getValue()),
                (first, second) -> first,
                HashMap::new
            ));
    }

    private RoleDTO toRoleDTO(Role role) {
        if (role instanceof Founder founder) {
            return new FounderDTO();
        }
        if (role instanceof Owner owner) {
            return new OwnerDTO(owner);
        }
        if (role instanceof Manager manager) {
            return new ManagerDTO(manager);
        }
        if (role instanceof Member member) {
            return new MemberDTO();
        }

        throw new IllegalArgumentException("Unsupported role type: " + role.getClass().getName());
    }
}
