package com.group16b.DomainLayer.User;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.group16b.DomainLayer.User.Roles.Manager;
import com.group16b.DomainLayer.User.Roles.ManagerPermissions;
import com.group16b.DomainLayer.User.Roles.Role;
import com.group16b.DomainLayer.User.Roles.RoleType;
import com.group16b.DomainLayer.User.Records.CompanyAssigmentKey;
import java.security.MessageDigest;

public class User {

	private int userID;
	private String email;
	private String password;
	private HashMap<Integer, Role> roles; // Key: companyID, Value: Role
	private final Map<CompanyAssigmentKey, Manager> userInvites; // Key: companyID, Value: List of Managers who invited the user

	private final ReentrantLock userInvitesLock;
	public User(String email, String password) {
		this.email = email;
		setPassword(password);
		this.roles = new HashMap<>();
		this.userInvites = new ConcurrentHashMap<>();
		this.userInvitesLock = new ReentrantLock();
	}

	public String getEmail() {
		return email;
	}

	public Role getRole(int companyID) {
		return roles.get(companyID);
	}

	public void setPassword(String newPassword) {
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

	public void addRole(int companyID, Role role) {
		roles.put(companyID, role);
	}


	//*
	// this method really needs a redesign or at least clarity notes
	// like what if caller is founder, do we return null or this
	// why do we return user instead of ID, since according to lecture 1, aggragate doesnt contains its own repo
	// this may also lead to how we model the hierarchy, but ill whoever uses it to figure that out */
	public User getParentIDForCompany(int companyID) {
		Role role = roles.get(companyID);
		if (role != null && role instanceof Manager) {
			Integer parentID = ((Manager) role).getAssignerID();
			if(parentID==null) return this; // No parent, this is the founder
			return IUserRepository.getInstance().getUserByID(parentID);
		}
		return null; // No role for this company, hence no parent
	}

	public int getUserID() {
		return userID;
	}

	public void validatePermissions(int companyID, Class<? extends Role> requiredRole) {
		// implement permission validation logic here
		// throws exception if user does not have required permissions
		return;
	}

	//probably the correct version, maybe add a set variant, as for managers we want to ensure perm is correct, not only role
	public void validatePermission(int companyID, ManagerPermissions permission) {
		// implement permission validation logic here
		// throws exception if user does not have required permissions
		return;
	}


	//adds an invite to a company
	//one invite per company and owner can exist
	public void addInvite(int companyID, int assignerID, Manager offeredRole) {
			userInvites.put(new CompanyAssigmentKey(companyID, assignerID), offeredRole);
	}

	//accepts an invite to be an owner
	//after that all other invites are useless so remove them
	public void acceptOwnerInvite(int companyID, int assignerID) {
			CompanyAssigmentKey key = new CompanyAssigmentKey(companyID, assignerID);
			Manager offeredRole = userInvites.get(key);
			if (offeredRole != null) {
				addRole(companyID, offeredRole);
				removeAllInvitesForCompany(companyID);
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
	public ReentrantLock getUserInvitesLock() {
		return userInvitesLock;
	}
}
