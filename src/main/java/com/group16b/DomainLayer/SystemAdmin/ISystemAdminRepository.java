package com.group16b.DomainLayer.SystemAdmin;

public interface ISystemAdminRepository {
    /*
     * Adds a system administrator to the repository.
     * @param systemAdmin the system administrator to add
     * @throws IllegalArgumentException if the system administrator is null or already exists
     */
    public void addSystemAdmin(SystemAdmin systemAdmin);

    /*
     * Retrieves a system administrator by their ID.
     * @param id the ID of the system administrator to retrieve
     * @return the system administrator with the specified ID, or null if not found
     */
    public SystemAdmin getSystemAdminById(int id);

    /*
     * Retrieves a system administrator by their username.
     * @param username the username of the system administrator to retrieve
     * @return the system administrator with the specified username, or null if not found
     */
    public SystemAdmin getSystemAdminByUsername(String username);
}
