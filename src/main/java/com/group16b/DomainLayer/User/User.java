package com.group16b.DomainLayer.User;


import java.security.MessageDigest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
@Entity
@Table(name = "users")
public class User {

	@Id
    @Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String password;

	@Version
	private long version;

	protected User() {}//required for jpa

	public User(String email, String password) {
		this.email = email;
		setPassword(password);
	}

	public User(User user) {
		this.email = user.getEmail();//changed for mocked testing userRepo
		this.password = user.password;
		this.version = user.getVersion();
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

		if (confirmPassword(newPassword)) {
            throw new IllegalArgumentException("New password cannot be the same as the old password.");
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

		public void incrementVersion() {
		this.version++;
	}

    public void update(User newUser) {
		this.email = newUser.email;
		this.password = newUser.password;
		
    }

    public void setVersion(long newVersion) {
		this.version = newVersion;
    }

}
