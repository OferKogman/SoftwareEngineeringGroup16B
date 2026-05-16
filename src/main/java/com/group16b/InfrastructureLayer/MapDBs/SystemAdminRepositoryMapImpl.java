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
	public void save(SystemAdmin systemAdmin) {
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
