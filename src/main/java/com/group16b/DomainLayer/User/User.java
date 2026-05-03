package com.group16b.DomainLayer.User;

import java.util.HashMap;

import com.group16b.DomainLayer.User.Roles.Manager;
import com.group16b.DomainLayer.User.Roles.Role;

public class User {

	private int userID;
	private String email;
	private String password;
	private HashMap<Integer, Role> roles; // Key: companyID, Value: Role

	protected User(String email, String password) {
		this.email = email;
		this.password = password;
		this.roles = new HashMap<>();
	}

	protected String getEmail() {
		return email;
	}

	protected Role getRole(int companyID) {
		return roles.get(companyID);
	}

	protected void addRole(int companyID, Role role) {
		roles.put(companyID, role);
	}

	protected User getParentForCompany(int companyID) {
		Role role = roles.get(companyID);
		if (role != null && role instanceof Manager) {
			int parentID = ((Manager) role).getParentID();
			return IUserRepository.getInstance().getUserByID(parentID);
		}
		return null; // No role for this company, hence no parent
	}

	protected int getUserID() {
		return userID;
	}
}
