package com.group16b.InfrastructureLayer.MapDBs;

import java.util.List;
import java.util.Map;

import org.springframework.dao.OptimisticLockingFailureException;

import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;
import org.springframework.stereotype.Repository;

@Repository
public class SystemAdminRepositoryMapImpl implements IRepository<SystemAdmin> {
	private Map<String, SystemAdmin> systemAdminsByUsername;



	public SystemAdminRepositoryMapImpl() {
		this.systemAdminsByUsername = new java.util.HashMap<>();
	}
	public SystemAdminRepositoryMapImpl(Map<String, SystemAdmin> systemAdminsByUsername) {
		this.systemAdminsByUsername = systemAdminsByUsername;
	}


	@Override
	public SystemAdmin findByID(String username) {
		SystemAdmin admin = systemAdminsByUsername.get(username);
		if(admin == null) {
			throw new IllegalArgumentException("System admin with username " + username + " does not exist.");
		}
		return (new SystemAdmin(admin));
	}

	@Override
	public List<SystemAdmin> getAll() {
		List<SystemAdmin> admins = new java.util.ArrayList<>();
		for(SystemAdmin admin : systemAdminsByUsername.values()) {
			admins.add(new SystemAdmin(admin));
		}
		return admins;
	}
	@Override
	public synchronized void delete(String username) {
		SystemAdmin admin = systemAdminsByUsername.remove(username);
	}

	public synchronized void save(SystemAdmin systemAdmin) {
		SystemAdmin existingAdmin = systemAdminsByUsername.get(systemAdmin.getUsername());
		if (existingAdmin != null) { //if admin exists in the system, update it
			long newVersion = systemAdmin.getVersion();
			long currentVersion = existingAdmin.getVersion();
			if (newVersion != currentVersion) {
				throw new OptimisticLockingFailureException("Version mismatch: expected " + currentVersion + " but got " + newVersion);
			}
			existingAdmin.updateAdmin(systemAdmin);
			systemAdminsByUsername.put(systemAdmin.getUsername(), existingAdmin);
			
		}
		else{ //if admin does not exist, add it to the system, no need to check versions because it's a new admin
			systemAdmin.setVersion(systemAdmin.getVersion() + 1);
			systemAdminsByUsername.put(systemAdmin.getUsername(), systemAdmin);
		}
		
	}

	

}
