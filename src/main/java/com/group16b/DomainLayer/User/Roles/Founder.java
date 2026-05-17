package com.group16b.DomainLayer.User.Roles;

public class Founder extends Owner {
	public Founder(int userID) {
		super(userID, null,RoleType.FOUNDER);
	}

}
