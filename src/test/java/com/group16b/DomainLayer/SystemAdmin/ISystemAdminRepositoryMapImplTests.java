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
        SystemAdmin admin = new SystemAdmin(1, "admin", "hashedpassword");
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

}
