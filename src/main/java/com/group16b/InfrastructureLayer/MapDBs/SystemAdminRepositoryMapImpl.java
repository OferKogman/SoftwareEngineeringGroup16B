package com.group16b.InfrastructureLayer.MapDBs;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;

@Repository
public class SystemAdminRepositoryMapImpl implements IRepository<SystemAdmin> {
	private Map<String, SystemAdmin> systemAdminsByUsername;



	public SystemAdminRepositoryMapImpl() {
		this.systemAdminsByUsername = new ConcurrentHashMap<>();
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
		systemAdminsByUsername.remove(username);
	}

	public synchronized void save(SystemAdmin systemAdmin) {
        if(!isValid(systemAdmin)) {
            throw new IllegalArgumentException("System admin data is invalid.");
        }
        SystemAdmin existingAdmin = systemAdminsByUsername.get(systemAdmin.getUsername());
        
        if (existingAdmin != null) {
            long currentVersion = existingAdmin.getVersion();
            
            if (systemAdmin.getVersion() != currentVersion) {
                throw new OptimisticLockingFailureException("Version mismatch: expected " + currentVersion + " but got " + systemAdmin.getVersion());
            }
            
            systemAdmin.setVersion(currentVersion + 1);
            
            SystemAdmin adminToUpdate = new SystemAdmin(systemAdmin);
            systemAdminsByUsername.put(systemAdmin.getUsername(), adminToUpdate);
            
        }
        else {
            systemAdmin.setVersion(systemAdmin.getVersion() + 1);
            
            SystemAdmin adminToStore = new SystemAdmin(systemAdmin);
            systemAdminsByUsername.put(systemAdmin.getUsername(), adminToStore);
        }
    }
	private boolean isValid(SystemAdmin systemAdmin) {
		if(systemAdmin == null) {
			return false;
		}
		if(systemAdmin.getUsername() == null || systemAdmin.getUsername().isEmpty()) {
			return false;
		}
		if(systemAdmin.getEmail() == null || systemAdmin.getEmail().isEmpty()) {
			return false;
		}
		if(systemAdmin.isPasswordSet() == false) {
			return false;
		}
		return true;
	}

	

}