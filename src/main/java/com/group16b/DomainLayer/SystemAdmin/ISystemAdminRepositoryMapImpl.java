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

    public static synchronized ISystemAdminRepositoryMapImpl getInstance() {
		return instance;
	}

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

    @Override
    public SystemAdmin getSystemAdminById(int id) {
        return systemAdminsById.get(id);
    }

    @Override
    public SystemAdmin getSystemAdminByUsername(String username) {
        return systemAdminsByUsername.get(username);
    }
    
}
