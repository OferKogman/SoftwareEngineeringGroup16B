package com.group16b.DomainLayer.User;

import java.util.HashMap;
import com.group16b.DomainLayer.User.Roles.Role;

public class User {

	private int userID;
	private String email;
	private String password;
	private HashMap<Integer, Role> roles; // Key: companyID, Value: Role

	public User(String email, String password) {
		this.email = email;
		this.password = password;
		this.roles = new HashMap<>();
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public Role getRole(int companyID) {
		return roles.get(companyID);
	}

	public void addRole(int companyID, Role role) {
		roles.put(companyID, role);
	}

	public User getParentForCompany(int companyID) {
		Role role = roles.get(companyID);
		if (role != null) {
			int parentID = role.getParentID();
			return IUserRepository.getInstance().getUserByID(parentID);
		}
		return null; // No role for this company, hence no parent
	}

	public int getUserID() {
		return userID;
	}
}
