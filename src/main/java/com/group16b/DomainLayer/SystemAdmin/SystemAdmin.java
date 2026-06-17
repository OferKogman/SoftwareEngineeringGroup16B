package com.group16b.DomainLayer.SystemAdmin;

import java.security.MessageDigest;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;


import java.nio.charset.StandardCharsets;

@Entity
@Table(name = "system_admins")
public class SystemAdmin {

    @Id
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Version
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

	public SystemAdmin() {
		// Default constructor for JPA
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
			if(newPassword == null || newPassword.isEmpty()) {
				throw new IllegalArgumentException("Password cannot be null or empty.");
			}
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
	public boolean isPasswordSet() {
		return this.password != null && !this.password.isEmpty();
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