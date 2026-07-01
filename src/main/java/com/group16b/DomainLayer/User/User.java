package com.group16b.DomainLayer.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

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

	protected User() {
	}// required for jpa

	public User(String email, String password) {
		this.email = email;
		setPassword(password);
	}

	public User(User user) {
		this.email = user.getEmail();// changed for mocked testing userRepo
		this.password = user.password;
		this.version = user.getVersion();
	}

	public String getEmail() {
		return email;
	}

	private void setPassword(String newPassword) {
		this.password = hashPassword(newPassword);
	}

	private String hashPassword(String password) {
		try {
			byte[] hash = MessageDigest
					.getInstance("SHA-256")
					.digest(password.getBytes(StandardCharsets.UTF_8));

			return HexFormat.of().formatHex(hash);
		} catch (Exception e) {
			throw new RuntimeException("Error hashing password", e);
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
		return this.password.equals(hashPassword(password));
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