package com.group16b.DomainLayer.User;


import java.security.MessageDigest;

public class User {
	private static int idCounter = 1;

	private int userID;
	private String email;
	private String password;

	public User(String email, String password) {
		this.userID = idCounter++;
		this.email = email;
		setPassword(password);
	}

	public String getEmail() {
		return email;
	}
	public int getUserID()
	{
		return userID;
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
	
	public void changePassword(String oldPassword, String newPassword) {
		
		if (!confirmPassword(oldPassword)) {
			throw new IllegalArgumentException("Old password is incorrect.");
		}
		setPassword(newPassword);
		
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
