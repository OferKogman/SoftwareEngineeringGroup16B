package com.group16b.ApplicationLayer.DTOs.Roles;

import com.group16b.DomainLayer.User.Roles.RoleType;

public class FounderDTO extends OwnerDTO{
    public FounderDTO() {
		super(null);
		this.roleType = RoleType.FOUNDER;
	}
}
