package com.group16b.DomainLayer.User.Roles;

public abstract class Role {
	protected Role() {
	private final int userID;
	protected Role(int userID) {
		this.userID = userID;
	}

	public int getUserID() {
		return this.userID;
	}
}
