package com.group16b.DomainLayer.User.Roles;

import java.util.Set;

public class Manager extends Member {
	private Integer assignerID;
	private Set<ManagerPermissions> permissions;
	private final RoleType roleType;

	protected Manager(int userID, Integer assignerID, Set<ManagerPermissions> permissions, RoleType role) {
		super(userID);
		this.assignerID = assignerID;
		this.permissions = Set.copyOf(permissions);
		this.roleType = role;
	}

	public Manager(int userID, Integer assignerID, Set<ManagerPermissions> permissions) {
		this(userID, assignerID,permissions,RoleType.MANAGER);
	}

	public Integer getAssignerID() {
			return assignerID;
	}

	public RoleType getRoleType() {
		return roleType;
	}

	public Set<ManagerPermissions> getPermissions() {
		return Set.copyOf(permissions);
	}
	
	//self exlanatory
	public void updatePermissions(Set<ManagerPermissions> perms)
	{
		if(perms==null || perms.isEmpty())
			throw new IllegalArgumentException("new permissions cannot be empty!");
		this.permissions=Set.copyOf(perms);
	}

	protected void setParent(Integer newID)
	{
			assignerID=newID;
	}


}
