package com.group16b.ApplicationLayer.DTOs.Roles;

import java.util.EnumSet;

import com.group16b.DomainLayer.User.Roles.ManagerPermissions;
import com.group16b.DomainLayer.User.Roles.Owner;
import com.group16b.DomainLayer.User.Roles.RoleType;

public class OwnerDTO extends ManagerDTO{

    public OwnerDTO(Owner owner) {
		super(owner.getAssignerID(),EnumSet.allOf(ManagerPermissions.class));
		this.roleType = RoleType.OWNER;
	}
}
