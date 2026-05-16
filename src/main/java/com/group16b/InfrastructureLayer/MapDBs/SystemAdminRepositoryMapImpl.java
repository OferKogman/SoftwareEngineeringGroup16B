package com.group16b.InfrastructureLayer.MapDBs;

import java.util.List;
import java.util.Map;

import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.SystemAdmin.ISystemAdminRepository;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;

public class SystemAdminRepositoryMapImpl implements IRepository {
	private Map<String, SystemAdmin> systemAdminsById;
	private Map<String, SystemAdmin> systemAdminsByUsername;



	public SystemAdminRepositoryMapImpl() {
		this.systemAdminsById = new java.util.HashMap<>();
		this.systemAdminsByUsername = new java.util.HashMap<>();
	}
	public SystemAdminRepositoryMapImpl(Map<String, SystemAdmin> systemAdminsById,  Map<String, SystemAdmin> systemAdminsByUsername) {
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
	public SystemAdmin getSystemAdminById(String newAdminID) {
		return systemAdminsById.get(newAdminID);
	}

	// Retrieves a system admin by their username.
	public SystemAdmin getSystemAdminByUsername(String username) {
		return systemAdminsByUsername.get(username);
	}

	public boolean doesSystemAdminExist(int adminID){
		return systemAdminsById.containsKey(adminID);
	}

	@Override
	public Object findByID(String ID) {
		return (new SystemAdmin(systemAdminsById.get(ID)));
	}

	@Override
	public List getAll() {
		List<SystemAdmin> admins = new java.util.ArrayList<>();
		for(SystemAdmin admin : systemAdminsById.values()) {
			admins.add(new SystemAdmin(admin));
		}
		return admins;
	}
	@Override
	public void delete(String ID) {
		SystemAdmin admin = systemAdminsById.remove(ID);
		if (admin != null) {
			systemAdminsByUsername.remove(admin.getUsername());
		}
	}

	@Override
	public void save(Object Obj) {
		SystemAdmin systemAdmin = (SystemAdmin) Obj;
		SystemAdmin existingAdmin = systemAdminsById.get(systemAdmin.getId());
		if (existingAdmin != null) {
			long newVersion = systemAdmin.getVersion();
			long currentVersion = existingAdmin.getVersion();
			if (newVersion != currentVersion) {
				throw new IllegalStateException("Version mismatch: expected " + currentVersion + " but got " + newVersion);
			}
			systemAdminsById.put(systemAdmin.getId(), new SystemAdmin(systemAdmin));
			existingAdmin.updateAdmin(systemAdmin);
		}
		
	}
	

}
