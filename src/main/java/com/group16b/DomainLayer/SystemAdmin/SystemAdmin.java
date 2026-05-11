package com.group16b.DomainLayer.SystemAdmin;

import java.security.MessageDigest;

public class SystemAdmin {
	private int id;
	private String username;
	private String password;

	public SystemAdmin(int id, String username, String password, String email) {
		this.id = id;
		this.username = username;
		setPassword(password);
	}

	public int getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}
	public String getEmail() {
		return username;
	}

	private void setPassword(String newPassword) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(newPassword.getBytes());
			String stringHash = new String(messageDigest.digest());
			this.password = stringHash;
		} catch (Exception e) {
			System.out.println("Error hashing password: " + e.getMessage());
		}
	}

	public boolean confirmPassword(String password) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(password.getBytes());
			String stringHash = new String(messageDigest.digest());
			return this.password.equals(stringHash);
		} catch (Exception e) {
			System.out.println("Error hashing password: " + e.getMessage());
			return false;	
		}
	}



}
