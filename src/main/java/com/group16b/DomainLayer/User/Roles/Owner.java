package com.group16b.DomainLayer.User.Roles;

import java.util.EnumSet;

public class Owner extends Manager {

	public Owner(Integer parentID) {
		super(parentID,EnumSet.allOf(ManagerPermissions.class));
		this.roleType = RoleType.OWNER;
	}

}
