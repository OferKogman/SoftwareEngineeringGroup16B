package com.group16b.DomainLayer.SystemAdmin;

import java.security.MessageDigest;

import java.util.List;

import com.group16b.DomainLayer.Event.Event;


public class SystemAdmin {
	private String username;
	private String password;
	private String email;
	private long version;

	public SystemAdmin(String username, String password, String email) {
		this.username = username;
		this.email = email;
		version = 0;
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		
		SystemAdmin other = (SystemAdmin) obj;
		return (this.username.equals(other.username) &&
				this.password.equals(other.password) &&
				this.email.equals(other.email));
	}

	/*public void closeProductionCompany(int productionCompanyId) {
		ProductionCompanyRepositoryMapImpl productionCompanyRepo = ProductionCompanyRepositoryMapImpl.getInstance();
		ProductionCompany company = productionCompanyRepo.findByID(String.valueOf(productionCompanyId));
		
		if(company == null) {
			System.out.println("Production company with ID " + productionCompanyId + " does not exist.");
			return;
		}

		EventRepositoryMapImpl eventRepo = EventRepositoryMapImpl.getInstance();
		List<Integer> productionCompanyIDs = new LinkedList<>();
		productionCompanyIDs.add(productionCompanyId);

		List<Event> companyEvents = eventRepo.searchEvents(null, null, null, null, null, null, null, null, null, productionCompanyIDs);
		List<User> companyUsers = company.getAssociatedUsers();
		try{
			if(!companyEvents.isEmpty()) {
				deactivateEvents(companyEvents);
			}

			if(!companyUsers.isEmpty()) {
				deactivateUsers(companyUsers, productionCompanyId);
			}
			productionCompanyRepo.removeProductionCompany(productionCompanyId);
		}
		catch(Exception e) {
			System.out.println("Error closing production company: " + e.getMessage());
		}

	}
		*/

	public long getVersion()
	{
		return version;
	}
	public void setVersion(long version)
	{
		this.version=version;
	}

	public void updateAdmin(SystemAdmin other)
	{

	}


}
