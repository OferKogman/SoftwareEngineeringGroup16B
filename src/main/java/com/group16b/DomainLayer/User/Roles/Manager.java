package com.group16b.DomainLayer.User.Roles;

import java.util.Set;

public class Manager extends Member {
	private Integer assignerID;
	private Set<ManagerPermissions> permissions;
	private final RoleType roleType;

	protected final Object lock=new Object();

	protected Manager(Integer assignerID, Set<ManagerPermissions> permissions, RoleType role) {
		this.assignerID = assignerID;
		this.permissions = Set.copyOf(permissions);
		this.roleType = role;
	}

	public Manager(Integer assignerID, Set<ManagerPermissions> permissions) {
		this(assignerID,permissions,RoleType.MANAGER);
	}

	public Integer getAssignerID() {
		synchronized(lock){
			return assignerID;
		}
	}

	public RoleType getRoleType() {
		return roleType;
	}

	public Set<ManagerPermissions> getPermissions() {
		return permissions;
	}
	
	protected void setParent(Integer newID)
	{
		synchronized(lock)
		{
			assignerID=newID;
		}
	}


}
