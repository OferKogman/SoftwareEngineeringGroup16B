package com.group16b.InfrastructureLayer.MapDBs;

import java.util.Map;

import com.group16b.DomainLayer.SystemAdmin.ISystemAdminRepository;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;

public class SystemAdminRepositoryMapImpl implements ISystemAdminRepository {
	private Map<Integer, SystemAdmin> systemAdminsById;
	private Map<String, SystemAdmin> systemAdminsByUsername;



	public SystemAdminRepositoryMapImpl() {
		this.systemAdminsById = new java.util.HashMap<>();
		this.systemAdminsByUsername = new java.util.HashMap<>();
	}
	public SystemAdminRepositoryMapImpl(Map<Integer, SystemAdmin> systemAdminsById,  Map<String, SystemAdmin> systemAdminsByUsername) {
		this.systemAdminsById = systemAdminsById;
		this.systemAdminsByUsername = systemAdminsByUsername;
	}


	/*
	 * Adds a system admin to the repository.
	 * 
	 * @param systemAdmin the system admin to add
	 * 
	 * @throws IllegalArgumentException if the system admin is null or if an admin
	 * with the same ID or username already exists
	 */
	@Override
	public void addSystemAdmin(SystemAdmin systemAdmin) {
		if (systemAdmin == null) {
			throw new IllegalArgumentException("SystemAdmin cannot be null");
		}
		if (systemAdminsById.containsKey(systemAdmin.getId())) {
			throw new IllegalArgumentException("SystemAdmin with this ID already exists");
		}
		if (systemAdminsByUsername.containsKey(systemAdmin.getUsername())) {
			throw new IllegalArgumentException("SystemAdmin with this username already exists");
		}
		systemAdminsById.put(systemAdmin.getId(), systemAdmin);
		systemAdminsByUsername.put(systemAdmin.getUsername(), systemAdmin);
	}

	// Retrieves a system admin by their ID.
	@Override
	public SystemAdmin getSystemAdminById(int id) {
		return systemAdminsById.get(id);
	}

	// Retrieves a system admin by their username.
	@Override
	public SystemAdmin getSystemAdminByUsername(String username) {
		return systemAdminsByUsername.get(username);
	}

	@Override
	public boolean doesSystemAdminExist(int adminID){
		return systemAdminsById.containsKey(adminID);
	}
	

}
