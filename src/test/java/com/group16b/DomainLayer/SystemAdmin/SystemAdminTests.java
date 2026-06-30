package com.group16b.DomainLayer.SystemAdmin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SystemAdminTests {

    private static final String USERNAME = "admin1";
    private static final String PASSWORD = "Password123!";
    private static final String EMAIL    = "admin@mail.ru";

    private SystemAdmin admin;

    @BeforeEach
    void setUp() {
        admin = new SystemAdmin(USERNAME, PASSWORD, EMAIL);
    }

    @Test
    void constructor_ValidData_StoresUsernameAndEmail() {
        assertEquals(USERNAME, admin.getUsername());
        assertEquals(EMAIL,    admin.getEmail());
    }

    @Test
    void constructor_ValidData_StartsAtVersionZero() {
        assertEquals(0, admin.getVersion());
    }

    @Test
    void constructor_ValidData_HashesPasswordOnCreation() {
        assertTrue(admin.confirmPassword(PASSWORD));
    }

    @Test
    void copyConstructor_ValidAdmin_CopiesAllFieldsExactly() {
        admin.setVersion(7L);
        SystemAdmin copy = new SystemAdmin(admin);

        assertEquals(admin.getUsername(), copy.getUsername());
        assertEquals(admin.getEmail(),    copy.getEmail());
        assertEquals(admin.getVersion(),  copy.getVersion());
        assertTrue(copy.confirmPassword(PASSWORD));
    }

    @Test
    void copyConstructor_MutatingCopyDoesNotAffectOriginal() {
        SystemAdmin copy = new SystemAdmin(admin);
        copy.setVersion(42L);

        assertEquals(0, admin.getVersion());
    }



    @Test
    void confirmPassword_CorrectPassword_ReturnsTrue() {
        assertTrue(admin.confirmPassword(PASSWORD));
    }

    @Test
    void confirmPassword_WrongPassword_ReturnsFalse() {
        assertFalse(admin.confirmPassword("WrongPassword!"));
    }

    @Test
    void confirmPassword_EmptyString_ReturnsFalse() {
        assertFalse(admin.confirmPassword(""));
    }


    @Test
    void equals_SameUsernamePasswordEmail_ReturnsTrue() {
        SystemAdmin other = new SystemAdmin(USERNAME, PASSWORD, EMAIL);

        assertTrue(admin.equals(other));
    }

    @Test
    void equals_DifferentUsername_ReturnsFalse() {
        SystemAdmin other = new SystemAdmin("differentAdmin", PASSWORD, EMAIL);

        assertFalse(admin.equals(other));
    }

    @Test
    void equals_DifferentPassword_ReturnsFalse() {
        SystemAdmin other = new SystemAdmin(USERNAME, "DifferentPass!", EMAIL);

        assertFalse(admin.equals(other));
    }

    @Test
    void equals_DifferentEmail_ReturnsFalse() {
        SystemAdmin other = new SystemAdmin(USERNAME, PASSWORD, "other@mail.ru");

        assertFalse(admin.equals(other));
    }

    @Test
    void equals_SameReference_ReturnsTrue() {
        assertTrue(admin.equals(admin));
    }

    @Test
    void equals_NullObject_ReturnsFalse() {
        assertFalse(admin.equals(null));
    }

    @Test
    void equals_DifferentClassObject_ReturnsFalse() {
        assertFalse(admin.equals("not an admin"));
    }

    @Test
    void equals_VersionDifferenceIsIgnored() {
        SystemAdmin other = new SystemAdmin(USERNAME, PASSWORD, EMAIL);
        other.setVersion(99L);

        assertTrue(admin.equals(other),
                "equals() must ignore version — only username, password, and email matter");
    }

    @Test
    void setVersion_ValidLong_UpdatesVersionDirectly() {
        admin.setVersion(99L);

        assertEquals(99L, admin.getVersion());
    }

    @Test
    void setVersion_Zero_VersionIsZero() {
        admin.setVersion(5L);  
        admin.setVersion(0L);

        assertEquals(0L, admin.getVersion());
    }


}