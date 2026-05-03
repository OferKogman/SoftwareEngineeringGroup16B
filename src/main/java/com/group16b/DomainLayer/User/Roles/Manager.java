package com.group16b.DomainLayer.User.Roles;

public class Manager extends Member {
	private int parentID;

	protected Manager(int parentID) {
		this.parentID = parentID;
	}

	public int getParentID() {
		return parentID;
	}
}
