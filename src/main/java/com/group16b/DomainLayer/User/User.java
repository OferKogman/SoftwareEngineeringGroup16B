package com.group16b.DomainLayer.User;


import java.security.MessageDigest;

public class User {

	private String email;
	private String password;
	private long version;

	public User(String email, String password) {
		this.email = email;
		setPassword(password);
		version = 0;
	}

	public User(User user) {
		this.email = user.email;
		this.password = user.password;
		this.version = user.version;
    }

    public String getEmail() {
		return email;
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

    public long getVersion() {
		return version;
    }

    public void update(User newUser) {
		this.email = newUser.email;
		this.password = newUser.password;
		this.version++;
		
    }

    public void setVersion(long newVersion) {
		this.version = newVersion;
    }

}
