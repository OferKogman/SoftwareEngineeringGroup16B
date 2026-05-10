package com.group16b.ApplicationLayer.DTOs.Roles;

import java.util.Set;

import com.group16b.DomainLayer.User.Roles.Manager;
import com.group16b.DomainLayer.User.Roles.ManagerPermissions;
import com.group16b.DomainLayer.User.Roles.RoleType;

public class ManagerDTO extends MemberDTO {
    private Integer assignerID;
	private Set<ManagerPermissions> permissions;
	protected RoleType roleType;

    public ManagerDTO(Integer assignerID, Set<ManagerPermissions> permissions) {
		this.assignerID = assignerID;
		this.permissions = Set.copyOf(permissions);
		this.roleType = RoleType.MANAGER;
	}

	public ManagerDTO(Manager manager) {
		this.assignerID = manager.getAssignerID();
		this.permissions = Set.copyOf(manager.getPermissions());
		this.roleType = RoleType.MANAGER;
	}

	public Integer getAssignerID() {
		return assignerID;
	}

	public RoleType getRoleType() {
		return roleType;
	}

    public Set<ManagerPermissions> getPermissions() {
		return permissions;
	}
}
