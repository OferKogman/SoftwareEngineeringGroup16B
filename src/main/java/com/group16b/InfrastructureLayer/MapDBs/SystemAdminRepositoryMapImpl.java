package com.group16b.InfrastructureLayer.MapDBs;

import java.util.List;
import java.util.Map;

import com.group16b.DomainLayer.SystemAdmin.ISystemAdminRepository;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;

public class SystemAdminRepositoryMapImpl implements ISystemAdminRepository {
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




	// Retrieves a system admin by their username.
	public SystemAdmin getSystemAdminByUsername(String username) {
		return systemAdminsByUsername.get(username);
	}

	public boolean doesSystemAdminExist(String adminID){
		return systemAdminsById.containsKey(adminID);
	}

	@Override
	public SystemAdmin findByID(String ID) {
		SystemAdmin admin = systemAdminsById.get(ID);
		if(admin == null) {
			return null;
		}
		return (new SystemAdmin(admin));
	}

	@Override
	public List<SystemAdmin> getAll() {
		List<SystemAdmin> admins = new java.util.ArrayList<>();
		for(SystemAdmin admin : systemAdminsById.values()) {
			admins.add(new SystemAdmin(admin));
		}
		return admins;
	}
	@Override
	public synchronized void delete(String ID) {
		SystemAdmin admin = systemAdminsById.remove(ID);
		if (admin != null) {
			systemAdminsByUsername.remove(admin.getUsername());
		}
	}

	@Override
	public synchronized void save(SystemAdmin systemAdmin) {
		SystemAdmin existingAdmin = systemAdminsById.get(systemAdmin.getId());
		if (existingAdmin != null) { //if admin exists in the system, update it
			long newVersion = systemAdmin.getVersion();
			long currentVersion = existingAdmin.getVersion();
			if (newVersion != currentVersion) {
				throw new IllegalArgumentException("Version mismatch: expected " + currentVersion + " but got " + newVersion);
			}
			existingAdmin.updateAdmin(systemAdmin);
			systemAdminsByUsername.put(systemAdmin.getUsername(), existingAdmin);
			systemAdminsById.put(systemAdmin.getId(), existingAdmin);
			
		}
		else{ //if admin does not exist, add it to the system, no need to check versions because it's a new admin
			systemAdmin.setVersion(systemAdmin.getVersion() + 1);
			systemAdminsById.put(systemAdmin.getId(), new SystemAdmin(systemAdmin));
			systemAdminsByUsername.put(systemAdmin.getUsername(), systemAdmin);
		}
		
	}

	

}
