package com.group16b.DomainLayer.SystemAdmin;

public interface ISystemAdminRepository {
    public void addSystemAdmin(SystemAdmin systemAdmin);
    public SystemAdmin getSystemAdminById(int id);
    public SystemAdmin getSystemAdminByUsername(String username);
}
