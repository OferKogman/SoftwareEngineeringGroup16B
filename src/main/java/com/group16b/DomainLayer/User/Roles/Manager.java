package com.group16b.DomainLayer.User.Roles;

import java.util.Set;

public class Manager extends Member {
	private int parentID;
	private Integer assignerID;
	private Set<ManagerPermissions> permissions;


	public Manager(Integer assignerID, Set<ManagerPermissions> permissions) {
		this.assignerID = assignerID;
		this.permissions = Set.copyOf(permissions);
	}

	public Integer getAssignerID() {
		return assignerID;
	}

	public Set<ManagerPermissions> getPermissions() {
		return permissions;
	}

	
}
