package com.group16b.ApplicationLayer.DTOs.Roles;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.group16b.DomainLayer.User.Roles.Manager;
import com.group16b.DomainLayer.User.Roles.ManagerPermissions;
import com.group16b.DomainLayer.User.Roles.Owner;
import com.group16b.DomainLayer.User.Roles.RoleType;

public class OwnerDTO extends ManagerDTO{

    private final List<Manager> assignedManagers;

    public OwnerDTO(Owner owner) {
		super(owner.getAssignerID(),EnumSet.allOf(ManagerPermissions.class));
		this.roleType = RoleType.OWNER;
        assignedManagers= new ArrayList<>();
	}
}
