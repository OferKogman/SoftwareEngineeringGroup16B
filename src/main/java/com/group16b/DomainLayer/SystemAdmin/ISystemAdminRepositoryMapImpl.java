package com.group16b.DomainLayer.SystemAdmin;

import java.util.HashMap;
import java.util.Map;

public class ISystemAdminRepositoryMapImpl implements ISystemAdminRepository {
    private Map<Integer, SystemAdmin> systemAdminsById;
    private Map<String, SystemAdmin> systemAdminsByUsername;

    private static final ISystemAdminRepositoryMapImpl instance = new ISystemAdminRepositoryMapImpl();

    private ISystemAdminRepositoryMapImpl() {
        this.systemAdminsById = new HashMap<>();
        this.systemAdminsByUsername = new HashMap<>();
    }

    //singleton pattern to ensure only one instance of the repository exists
    public static synchronized ISystemAdminRepositoryMapImpl getInstance() {
		return instance;
	}

    /*
     * Adds a system admin to the repository.
     * @param systemAdmin the system admin to add
     * @throws IllegalArgumentException if the system admin is null or if an admin with the same ID or username already exists
     */
    @Override
    public void addSystemAdmin(SystemAdmin systemAdmin) {
        if(systemAdmin == null) {
            throw new IllegalArgumentException("SystemAdmin cannot be null");
        }
        if(systemAdminsById.containsKey(systemAdmin.getId())) {
            throw new IllegalArgumentException("SystemAdmin with this ID already exists");
        }
        if(systemAdminsByUsername.containsKey(systemAdmin.getUsername())) {
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
    
}
