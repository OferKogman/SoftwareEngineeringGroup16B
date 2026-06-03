package com.group16b.DomainLayer.SystemAdmin;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class SystemAdmin {
	private String username;
	private String password;
	private String email;
	private long version;

	public SystemAdmin(String username, String password, String email) {
		this.username = username;
		this.email = email;
		this.version = 0;
		setPassword(password);
	}

	public SystemAdmin(SystemAdmin other) {
		this.username = other.username;
		this.password = other.password;
		this.email = other.email;
		this.version = other.version;
	}



	public String getUsername() {
		return username;
	}

	public String getEmail() {
		return email;
	}

	public long getVersion() {
		return version;
	}



	public void setVersion(long version) {
		this.version = version;
	}




	private void setPassword(String newPassword) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(newPassword.getBytes(StandardCharsets.UTF_8));
			this.password = new String(md.digest(), StandardCharsets.ISO_8859_1);
			this.version++; // Increment version whenever password is set/changed
		} catch (Exception e) {
			throw new RuntimeException("Error hashing password: " + e.getMessage(), e);
		}
	}


	public boolean confirmPassword(String password) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(password.getBytes(StandardCharsets.UTF_8));
			String hash = new String(md.digest(), StandardCharsets.ISO_8859_1);
			return this.password.equals(hash);
		} catch (Exception e) {
			throw new RuntimeException("Error hashing password: " + e.getMessage(), e);
		}
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		SystemAdmin other = (SystemAdmin) obj;
		return this.username.equals(other.username)
				&& this.password.equals(other.password)
				&& this.email.equals(other.email);
	}

}