package com.group16b.DomainLayer.User.Roles;

import java.util.Set;

public class Manager extends Member {
	private Integer assignerID;
	private Set<ManagerPermissions> permissions;
	protected RoleType roleType;


	public Manager(Integer assignerID, Set<ManagerPermissions> permissions) {
		this.assignerID = assignerID;
		this.permissions = Set.copyOf(permissions);
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
