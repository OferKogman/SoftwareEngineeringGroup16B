package com.group16b.DomainLayer.User;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.group16b.DomainLayer.User.Roles.Founder;
import com.group16b.DomainLayer.User.Roles.Manager;
import com.group16b.DomainLayer.User.Roles.ManagerPermissions;
import com.group16b.DomainLayer.User.Roles.Owner;
import com.group16b.DomainLayer.User.Roles.Role;
import com.group16b.DomainLayer.User.Roles.RoleType;
import com.group16b.DomainLayer.User.Records.CompanyAssigmentKey;

import java.security.MessageDigest;

public class User {
	private static int idCounter = 1;

	private int userID;
	private String email;
	private String password;
	private HashMap<Integer, Role> roles; // Key: companyID, Value: Role
	private final Map<CompanyAssigmentKey, Manager> userInvites; // Key: companyID, Value: List of Managers who invited the user

	public User(String email, String password) {
		this.userID = idCounter++;
		this.email = email;
		setPassword(password);
		this.roles = new HashMap<>();
		this.userInvites = new ConcurrentHashMap<>();
	}

	public String getEmail() {
		return email;
	}

	public Role getRole(int companyID) {
		return roles.get(companyID);
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

	public void addRole(int companyID, Role role) {
		roles.put(companyID, role);
	}

	public void removeRole(int companyID) {
		roles.remove(companyID);
	}
	public HashMap<Integer, Role> getRoles() {
		return new HashMap<>(this.roles);
	}


	//*
	// this method really needs a redesign or at least clarity notes
	// if caller is founder return -1, if no role return null
	// why do we return user instead of ID, since according to lecture 1, aggragate doesnt contains its own repo */
	public Integer getParentIDForCompany(int companyID) {
		Role role = roles.get(companyID);
		if (role != null && role instanceof Manager) {
			int parentID = ((Manager) role).getAssignerID();
			return parentID;
		}
		if (role != null && role instanceof Owner) {
			int parentID = ((Owner) role).getAssignerID();
			return parentID;
		}
		if (role != null && role instanceof Founder) {
			return -1;
		}
		return null; // No role for this company, hence no parent
	}

	public int getUserID() {
		return userID;
	}

	//validates role permissions is high enough for a specific company
	//hierarchy: founder > owner > manager > member no idea what is member
	public void validatePermissions(int companyID, Class<? extends Role> requiredRole) {
		
		if (!managerInCompany(companyID)) {
			throw new IllegalArgumentException(
				"User does not have a role for this company."
			);
		}

		Role role = getRole(companyID);
		if (!requiredRole.isAssignableFrom(role.getClass())) {
			throw new IllegalArgumentException(
				"User does not have sufficient permissions for this action."
			);
		}
	}
	//validates user have permission for a specific company to perform a specific action
	public void validatePermissions(int companyID, ManagerPermissions requiredPermission) {
		if (!managerInCompany(companyID)) {
			throw new IllegalArgumentException(
				"User does not have a role for this company."
			);
		}

		Role role = getRole(companyID);
		if(!((Manager) role).getPermissions().contains(requiredPermission)) {
			throw new IllegalArgumentException(
				"User does not have sufficient permissions for this action."
			);
		}
	}

	public boolean managerInCompany(int companyID) {
		Role role = getRole(companyID);
		return role != null && role instanceof Manager;
	}


	//adds an invite to a company
	//one invite per company and owner can exist
	public void addInvite(int companyID, int assignerID, Manager offeredRole) {
		if(!canGetAssignedRole(companyID, offeredRole.getClass())) {
			throw new IllegalArgumentException("User already has the requested role for this company.");
		}
		userInvites.put(new CompanyAssigmentKey(companyID, assignerID), offeredRole);
	}

	//checks if we can accept and hold to an invite
	//used ensure we dont invite an owner, or that we dont assign manager to an already manager
	private boolean canGetAssignedRole(int companyID, Class<? extends Role> role) {
		try
		{
			validatePermissions(companyID, role);
			return false;
		}
		catch (IllegalArgumentException e)
		{
			return true;
		}
	}

	//accept and invite to a company
	//after asigning, clear all the irelevant invites
	//also ensure user role is not already high enough
	public void acceptInvite(int companyID, int assignerID) {
		CompanyAssigmentKey key = new CompanyAssigmentKey(companyID, assignerID);
		Manager offeredRole = userInvites.get(key);
		
		if (offeredRole != null) {
			if(!canGetAssignedRole(companyID, offeredRole.getClass())) {
				throw new IllegalArgumentException("User already has this role for this company.");
			}
			//all clear, can add
			addRole(companyID, offeredRole);
			if(offeredRole.getRoleType() == RoleType.OWNER) {
				removeAllInvitesForCompany(companyID);
			} else if(offeredRole.getRoleType() == RoleType.MANAGER) {
				removeAllManagerInvitesForCompany(companyID);
			}
		} else {
			throw new IllegalArgumentException("No invite found for company ID " + companyID + " from assigner ID " + assignerID);
		}
	}

	public void rejectInvite(int companyID, int assignerID) {
		CompanyAssigmentKey key = new CompanyAssigmentKey(companyID, assignerID);
		Manager offeredRole = userInvites.get(key);
		
		if (offeredRole != null) {
			userInvites.remove(key);
		} else {
			throw new IllegalArgumentException("No invite found for company ID " + companyID + " from assigner ID " + assignerID);
		}
	}
	
	private void removeAllManagerInvitesForCompany(int companyID) {
			userInvites.entrySet().removeIf(entry -> entry.getKey().companyID() == companyID && entry.getValue().getRoleType() == RoleType.MANAGER);
	}

	private void removeAllInvitesForCompany(int companyID) {
			userInvites.entrySet().removeIf(entry -> entry.getKey().companyID() == companyID);
	}

	//add an asigneed under this owner
	public void addAssignee(int companyID, Manager manager) {
		Role role = getRole(companyID);
		if (role == null || !(role instanceof Owner)) {
			throw new IllegalArgumentException("User does not have permission to assign roles for this company.");
		}

		((Owner)role).assignManager(manager);
	}

	public boolean isOwnerOfCompany(int companyID) {
		Role role = getRole(companyID);
		return role != null && role instanceof Owner;
	}

}
