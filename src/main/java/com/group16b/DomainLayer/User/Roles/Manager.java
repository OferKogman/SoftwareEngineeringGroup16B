package com.group16b.DomainLayer.User.Roles;

import java.util.Set;

public class Manager extends Member {
	private Integer assignerID;
	private Set<ManagerPermissions> permissions;


	protected Manager(Integer assignerID, Set<ManagerPermissions> permissions) {
		this.assignerID = assignerID;
		this.permissions = permissions;
	}

	public Integer getAssignerID() {
		return assignerID;
	}

	public boolean hasPermission(ManagerPermissions permission) {
		return permissions.contains(permission);
	}

	public Set<ManagerPermissions> getPermissions() {
		return Set.copyOf(permissions);
	}


}
