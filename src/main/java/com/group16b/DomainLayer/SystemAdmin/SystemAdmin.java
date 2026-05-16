package com.group16b.DomainLayer.SystemAdmin;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.group16b.ApplicationLayer.EventService;
import com.group16b.ApplicationLayer.DTOs.EventDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.ILocatoinService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.DomainServices.EventFilteringService;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.User.User;
import com.group16b.InfrastructureLayer.AuthenticationServiceJWTImpl;
import com.group16b.InfrastructureLayer.LocationServicePhotonImpl;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;


public class SystemAdmin {
	private int id;
	private String username;
	private String password;
	private String email;

	public SystemAdmin(int id, String username, String password, String email) {
		this.id = id;
		this.username = username;
		this.email = email;
		setPassword(password);
	}

	public int getId() {
		return id;
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



	private void deactivateEvents(List<Event> events) {
		for (Event e : events) {
			e.deactivateEvent();
		}
	}

	private void deactivateUsers(List<User> users, int productionCompanyId) {
		for (User u : users) {
			u.removeRole(productionCompanyId);
		}
	}


}
