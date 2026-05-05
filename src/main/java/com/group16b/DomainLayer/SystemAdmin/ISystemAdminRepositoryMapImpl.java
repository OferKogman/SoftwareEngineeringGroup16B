package com.group16b.DomainLayer.SystemAdmin;

import java.util.HashMap;
import java.util.Map;

public class ISystemAdminRepositoryMapImpl implements ISystemAdminRepository {
    private Map<Integer, SystemAdmin> systemAdminsById;
    private Map<String, SystemAdmin> systemAdminsByUsername;

    public ISystemAdminRepositoryMapImpl() {
        this.systemAdminsById = new HashMap<>();
        this.systemAdminsByUsername = new HashMap<>();
    }

    @Override
    public void addSystemAdmin(SystemAdmin systemAdmin) {
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
