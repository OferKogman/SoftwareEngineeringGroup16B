package com.group16b.DomainLayer.User.Roles;
import com.group16b.DomainLayer.ProductionCompany.membership.RoleType;
public class Founder extends Owner {
	public Founder(int userID) {
		super(userID, null,RoleType.FOUNDER);
	}

}
