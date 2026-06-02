package com.group16b.infrastructureLayer.MapDBs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;

import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;
import com.group16b.InfrastructureLayer.MapDBs.SystemAdminRepositoryMapImpl;

public class SystemAdminRepositoryMapImplTests {
	SystemAdminRepositoryMapImpl repository;

	@BeforeEach
	void setUp() {
		repository = new SystemAdminRepositoryMapImpl();
	}

	@Test
	void testAddAndGetSystemAdminById() {
		SystemAdmin admin = new SystemAdmin( "admin1", "password", "email");
		repository.save(admin);
		SystemAdmin retrievedAdmin = repository.findByID("1");
		assertEquals(admin, retrievedAdmin);
	}

	@Test
	void testAddAndGetSystemAdminByUsername() {
		SystemAdmin admin = new SystemAdmin("admin2", "password2", "email2");
		repository.save(admin);
		SystemAdmin retrievedAdmin = repository.findByID("admin2");
		assertEquals(admin, retrievedAdmin);
	}

	@Test
	void testGetSystemAdminByIdNotFound() {
		assertThrows(IllegalArgumentException.class, () -> repository.findByID("999"));
	}

	@Test
	void testGetSystemAdminByUsernameNotFound() {
		SystemAdmin retrievedAdmin = repository.findByID("nonexistent");
		assertEquals(null, retrievedAdmin);
	}


	@Test
	void testAddMultipleSystemAdmins() {
		SystemAdmin admin3 = new SystemAdmin("admin3", "pass1", "email3");
		SystemAdmin admin4 = new SystemAdmin( "admin4", "pass2", "email4");
		repository.save(admin3);
		repository.save(admin4);

		assertEquals(admin3, repository.findByID("admin3"));
		assertEquals(admin4, repository.findByID("admin4"));
	}

	@Test
	void testAddSystemAdminWithDuplicateUsername() {
		SystemAdmin admin7 = new SystemAdmin("admin7", "pass1", "email7");
		SystemAdmin admin7duplicateUsername = new SystemAdmin("admin7", "pass2", "email8"); // same username
		repository.save(admin7);
		repository.save(admin7duplicateUsername); // should update the existing admin with the new details

		SystemAdmin retrievedByUsername = repository.findByID("admin7");
		assertEquals(admin7duplicateUsername, retrievedByUsername); // should be the second one added that was updated
	}

	@Test
	void testGetByIdAndUsernameReturnSameObject() {
		SystemAdmin admin = new SystemAdmin( "admin10", "pass", "email10");
		repository.save(admin);

		SystemAdmin byId = repository.findByID("10");
		SystemAdmin byUsername = repository.findByID("admin10");

		assertEquals(byId, byUsername);
		assertEquals(admin, byId);
	}
}
