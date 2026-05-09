package com.group16b.DomainLayer.User;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.DomainLayer.User.Roles.Owner;
public class UserTests {
    @Test
    void testAddingAssigningOwnerRequestAndAccepting()
    {
        User user = new User( "testuser", "hashedpassword");
        assertThrows(RuntimeException.class, () -> user.acceptOwnerInvite(0, 0));
        assertNull(user.getRole(0));
        user.addInvite(0, 0, new Owner(0));
        user.addInvite(0, 1, new Owner(1));
        user.addInvite(1, 1, new Owner(1));
        assertNull(user.getRole(0));
        assertDoesNotThrow(() -> user.acceptOwnerInvite(0, 0));
        assertEquals(Owner.class, user.getRole(0).getClass());
        assertThrows(RuntimeException.class, () -> user.acceptOwnerInvite(0, 0));
        assertDoesNotThrow(() -> user.acceptOwnerInvite(1, 1));
    }
}
