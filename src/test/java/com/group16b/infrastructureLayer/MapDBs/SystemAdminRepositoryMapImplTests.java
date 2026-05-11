package com.group16b.infrastructureLayer.MapDBs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;
import com.group16b.InfrastructureLayer.MapDBs.SystemAdminRepositoryMapImpl;

public class SystemAdminRepositoryMapImplTests {
	SystemAdminRepositoryMapImpl repository;

	@BeforeEach
	void setUp() {
		repository = SystemAdminRepositoryMapImpl.getInstance();
	}

	@Test
	void testAddAndGetSystemAdminById() {
		SystemAdmin admin = new SystemAdmin(1, "admin1", "password", "email");
		repository.addSystemAdmin(admin);
		SystemAdmin retrievedAdmin = repository.getSystemAdminById(1);
		assertEquals(admin, retrievedAdmin);
	}

	@Test
	void testAddAndGetSystemAdminByUsername() {
		SystemAdmin admin = new SystemAdmin(2, "admin2", "password2", "email2");
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
		SystemAdminRepositoryMapImpl instance1 = SystemAdminRepositoryMapImpl.getInstance();
		SystemAdminRepositoryMapImpl instance2 = SystemAdminRepositoryMapImpl.getInstance();
		assertEquals(instance1, instance2);
	}

	@Test
	void testAddMultipleSystemAdmins() {
		SystemAdmin admin3 = new SystemAdmin(3, "admin3", "pass1", "email3");
		SystemAdmin admin4 = new SystemAdmin(4, "admin4", "pass2", "email4");
		repository.addSystemAdmin(admin3);
		repository.addSystemAdmin(admin4);

		assertEquals(admin3, repository.getSystemAdminById(3));
		assertEquals(admin4, repository.getSystemAdminById(4));
		assertEquals(admin3, repository.getSystemAdminByUsername("admin3"));
		assertEquals(admin4, repository.getSystemAdminByUsername("admin4"));
	}

	@Test
	void testAddSystemAdminWithDuplicateId() {
		SystemAdmin admin5 = new SystemAdmin(5, "admin5", "pass1", "email5");
		SystemAdmin admin5duplicateId = new SystemAdmin(5, "admin6", "pass2", "email6"); // same id
		repository.addSystemAdmin(admin5);
		assertThrows(IllegalArgumentException.class, () -> repository.addSystemAdmin(admin5duplicateId));

		SystemAdmin retrievedById = repository.getSystemAdminById(5);
		assertEquals(admin5, retrievedById); // should still be the first one added
	}

	@Test
	void testAddSystemAdminWithDuplicateUsername() {
		SystemAdmin admin7 = new SystemAdmin(7, "admin7", "pass1", "email7");
		SystemAdmin admin7duplicateUsername = new SystemAdmin(8, "admin7", "pass2", "email8"); // same username
		repository.addSystemAdmin(admin7);
		assertThrows(IllegalArgumentException.class, () -> repository.addSystemAdmin(admin7duplicateUsername));

		SystemAdmin retrievedByUsername = repository.getSystemAdminByUsername("admin7");
		assertEquals(admin7, retrievedByUsername); // should still be the first one added
	}

	@Test
	void testGetByIdAndUsernameReturnSameObject() {
		SystemAdmin admin = new SystemAdmin(10, "admin10", "pass", "email10");
		repository.addSystemAdmin(admin);

		SystemAdmin byId = repository.getSystemAdminById(10);
		SystemAdmin byUsername = repository.getSystemAdminByUsername("admin10");

		assertEquals(byId, byUsername);
		assertEquals(admin, byId);
	}
}
