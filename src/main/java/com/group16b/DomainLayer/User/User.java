package com.group16b.DomainLayer.User;

import java.util.HashMap;

import com.group16b.DomainLayer.User.Roles.Manager;
import com.group16b.DomainLayer.User.Roles.Role;
import java.security.MessageDigest;

public class User {

	private int userID;
	private String email;
	private String password;
	private HashMap<Integer, Role> roles; // Key: companyID, Value: Role

	public User(String email, String password) {
		this.email = email;
		setPassword(password);
		this.roles = new HashMap<>();
	}

	public String getEmail() {
		return email;
	}

	public Role getRole(int companyID) {
		return roles.get(companyID);
	}

	public void setPassword(String newPassword) {
		try{
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(newPassword.getBytes());
			String stringHash = new String(messageDigest.digest());
			this.password = stringHash;
		}
		catch(Exception e){
			System.out.println("Error hashing password: " + e.getMessage());
		}
	}

	public boolean confirmPassword(String password){
		try{
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(password.getBytes());
			String stringHash = new String(messageDigest.digest());
			return this.password.equals(stringHash);
		}
		catch(Exception e){
			System.out.println("Error hashing password: " + e.getMessage());
			return false;
		}
	}
	public void addRole(int companyID, Role role) {
		roles.put(companyID, role);
	}

	public User getParentForCompany(int companyID) {
		Role role = roles.get(companyID);
		if (role != null && role instanceof Manager) {
			int parentID = ((Manager) role).getParentID();
			return IUserRepository.getInstance().getUserByID(parentID);
		}
		return null; // No role for this company, hence no parent
	}

	public int getUserID() {
		return userID;
	}
}
