package com.group16b.DomainLayer.SystemAdmin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/*
the most basic tests for the SystemAdmin class, just to make sure the getters are working correctly. The ISystemAdminRepositoryMapImplTests class will have more comprehensive tests for the repository implementation.
maybe will be exapned in the future to include more functionality in the SystemAdmin class, but for now it's just a simple data class with getters.
*/
class SystemAdminTests {
	SystemAdmin admin;

	@BeforeEach
	void setUp() {
		admin = new SystemAdmin("admin", "password", "email");
	}


	@Test
	void testGetUsername() {
		assertEquals("admin", admin.getUsername());
	}

}
