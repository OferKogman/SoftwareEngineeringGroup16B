package com.group16b.DomainLayer.User.Roles;

public abstract class Role {
	private final int userID;
	protected Role(int userID) {
		this.userID = userID;
	}

	public int getUserID() {
		return this.userID;
	}

	public int getUserId(){
		return -1;
	}
}
