package com.group16b.DomainLayer.SystemAdmin;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ISystemAdminRepositoryMapImplTests {
    ISystemAdminRepositoryMapImpl repository;

    @BeforeEach
    void setUp() {
        repository = ISystemAdminRepositoryMapImpl.getInstance();
    }

    @Test
    void testAddAndGetSystemAdminById() {
        SystemAdmin admin = new SystemAdmin(1, "admin1", "hashedpassword");
        repository.addSystemAdmin(admin);
        SystemAdmin retrievedAdmin = repository.getSystemAdminById(1);
        assertEquals(admin, retrievedAdmin);
    }

    @Test
    void testAddAndGetSystemAdminByUsername() {
        SystemAdmin admin = new SystemAdmin(2, "admin2", "hashedpassword2");
        repository.addSystemAdmin(admin);
        SystemAdmin retrievedAdmin = repository.getSystemAdminByUsername("admin2");
        assertEquals(admin, retrievedAdmin);
    }

    @Test
    void testGetSystemAdminByIdNotFound() {
        SystemAdmin retrievedAdmin = repository.getSystemAdminById(999);
        assertEquals(null, retrievedAdmin);
    }

    @Test
    void testGetSystemAdminByUsernameNotFound() {
        SystemAdmin retrievedAdmin = repository.getSystemAdminByUsername("nonexistent");
        assertEquals(null, retrievedAdmin);
    }

    @Test
    void testSingletonInstance() {
        ISystemAdminRepositoryMapImpl instance1 = ISystemAdminRepositoryMapImpl.getInstance();
        ISystemAdminRepositoryMapImpl instance2 = ISystemAdminRepositoryMapImpl.getInstance();
        assertEquals(instance1, instance2);
    }

    @Test
    void testAddMultipleSystemAdmins() {
        SystemAdmin admin3 = new SystemAdmin(3, "admin3", "pass1");
        SystemAdmin admin4 = new SystemAdmin(4, "admin4", "pass2");
        repository.addSystemAdmin(admin3);
        repository.addSystemAdmin(admin4);
        
        assertEquals(admin3, repository.getSystemAdminById(3));
        assertEquals(admin4, repository.getSystemAdminById(4));
        assertEquals(admin3, repository.getSystemAdminByUsername("admin3"));
        assertEquals(admin4, repository.getSystemAdminByUsername("admin4"));
    }

    @Test
    void testAddSystemAdminWithDuplicateId() {
        SystemAdmin admin5 = new SystemAdmin(5, "admin5", "pass1");
        SystemAdmin admin5duplicateId = new SystemAdmin(5, "admin6", "pass2"); // same id
        repository.addSystemAdmin(admin5);
        assertThrows(IllegalArgumentException.class, () -> repository.addSystemAdmin(admin5duplicateId));
 
        SystemAdmin retrievedById = repository.getSystemAdminById(5);
        assertEquals(admin5, retrievedById); // should still be the first one added
        assertEquals("pass1", retrievedById.getPasswordHash());
    }

    @Test
    void testAddSystemAdminWithDuplicateUsername() {
        SystemAdmin admin7 = new SystemAdmin(7, "admin7", "pass1");
        SystemAdmin admin7duplicateUsername = new SystemAdmin(8, "admin7", "pass2"); // same username
        repository.addSystemAdmin(admin7);
        assertThrows(IllegalArgumentException.class, () -> repository.addSystemAdmin(admin7duplicateUsername));
 
        SystemAdmin retrievedByUsername = repository.getSystemAdminByUsername("admin7");
        assertEquals(admin7, retrievedByUsername); // should still be the first one added
        assertEquals("pass1", retrievedByUsername.getPasswordHash());
    }

    @Test
    void testGetByIdAndUsernameReturnSameObject() {
        SystemAdmin admin = new SystemAdmin(10, "admin10", "pass");
        repository.addSystemAdmin(admin);
        
        SystemAdmin byId = repository.getSystemAdminById(10);
        SystemAdmin byUsername = repository.getSystemAdminByUsername("admin10");
        
        assertEquals(byId, byUsername);
        assertEquals(admin, byId);
    }
}
