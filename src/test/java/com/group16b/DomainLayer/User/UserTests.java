package com.group16b.DomainLayer.User;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.EnumSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.DomainLayer.User.Roles.Founder;
import com.group16b.DomainLayer.User.Roles.Manager;
import com.group16b.DomainLayer.User.Roles.ManagerPermissions;
import com.group16b.DomainLayer.User.Roles.Member;
import com.group16b.DomainLayer.User.Roles.Founder;
import com.group16b.DomainLayer.User.Roles.Manager;
import com.group16b.DomainLayer.User.Roles.Owner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.DomainLayer.User.Roles.Owner;
public class UserTests {
    @Test
    void testAddingAssigningOwnerRequestAndAccepting()
    {
        User user = new User( "testuser", "hashedpassword");
        assertThrows(RuntimeException.class, () -> user.acceptInvite(0, 0));
        assertNull(user.getRole(0));
        user.addInvite(0, 0, new Owner(0));
        user.addInvite(0, 1, new Owner(1));
        user.addInvite(1, 1, new Owner(1));
        assertNull(user.getRole(0));
        assertDoesNotThrow(() -> user.acceptInvite(0, 0));
        assertEquals(Owner.class, user.getRole(0).getClass());
        assertThrows(RuntimeException.class, () -> user.acceptInvite(0, 0));
        assertDoesNotThrow(() -> user.acceptInvite(1, 1));
    }
    @Test
    void testPromotionInviteAcceptance()
    {
        User user = new User( "testuser", "hashedpassword");
        assertThrows(RuntimeException.class, () -> user.acceptInvite(0, 0));
        assertNull(user.getRole(0));
        user.addInvite(0, 0, new Manager(0, EnumSet.allOf(ManagerPermissions.class)));
        user.addInvite(0, 1, new Manager(0, EnumSet.allOf(ManagerPermissions.class)));
        user.addInvite(0, 2, new Owner(0));
        user.addInvite(0, 3, new Manager(0, EnumSet.allOf(ManagerPermissions.class)));
        assertDoesNotThrow(() -> user.acceptInvite(0, 0));
        assertEquals(Manager.class, user.getRole(0).getClass());
        assertThrows(RuntimeException.class, () -> user.acceptInvite(0, 0));
        assertThrows(RuntimeException.class, () -> user.acceptInvite(0, 1));
        assertDoesNotThrow(() -> user.acceptInvite(0, 2));
        assertThrows(RuntimeException.class, () -> user.acceptInvite(0, 3));
    }


    @Test
    void testCheckingForUserRolePermissionsOwnerSuccess()
    {
        User user1 = new User( "testuser", "hashedpassword");
        user1.addRole(1, new Owner(0));
        assertDoesNotThrow(() -> user1.validatePermissions(1, Owner.class));
        assertDoesNotThrow(() -> user1.validatePermissions(1, Manager.class));
        assertThrows(IllegalArgumentException.class, () -> user1.validatePermissions(1, Founder.class));
    }
    @Test
    void testCheckingForUserRolePermissionsFounderSuccess()
    {
        User user1 = new User( "testuser", "hashedpassword");
        user1.addRole(1, new Founder());
        assertDoesNotThrow(() -> user1.validatePermissions(1, Founder.class));
        assertDoesNotThrow(() -> user1.validatePermissions(1, Owner.class));
        assertDoesNotThrow(() -> user1.validatePermissions(1, Manager.class));
    }
    @Test
    void testCheckingForUserRolePermissionsManagerSuccess()
    {
        User user1 = new User( "testuser", "hashedpassword");
        user1.addRole(1, new Manager(0, EnumSet.allOf(ManagerPermissions.class)));
        assertDoesNotThrow(() -> user1.validatePermissions(1, Manager.class));
        assertThrows(IllegalArgumentException.class, () -> user1.validatePermissions(1, Owner.class));
        assertThrows(IllegalArgumentException.class, () -> user1.validatePermissions(1, Founder.class));
    }

    @Test
    void testCheckingForUserWithNoRolePermissionsFailure()
    {
        User user1 = new User( "testuser", "hashedpassword");
        assertThrows(IllegalArgumentException.class, () -> user1.validatePermissions(2, Manager.class));
        assertThrows(IllegalArgumentException.class, () -> user1.validatePermissions(2, Owner.class));
        assertThrows(IllegalArgumentException.class, () -> user1.validatePermissions(2, Founder.class));
    }

    @Test
    void testForSinglePermissionManager()
    {
        User user1 = new User( "testuser", "hashedpassword");
        user1.addRole(1, new Manager(0, EnumSet.of(ManagerPermissions.EVENT_INVENTORY)));
        assertDoesNotThrow(() -> user1.validatePermissions(1, ManagerPermissions.EVENT_INVENTORY));
        assertThrows(IllegalArgumentException.class, () -> user1.validatePermissions(1, ManagerPermissions.PURCHASE_POLICY));
    }
    @Test
    void testForSinglePermissionOwnerSuccessForAll()
    {
        User user1 = new User( "testuser", "hashedpassword");
        user1.addRole(1, new Owner(0));
        assertDoesNotThrow(() -> user1.validatePermissions(1, ManagerPermissions.EVENT_INVENTORY));
        assertDoesNotThrow(() -> user1.validatePermissions(1, ManagerPermissions.PURCHASE_POLICY));
        assertDoesNotThrow(() -> user1.validatePermissions(1, ManagerPermissions.VENUE_CONFIGURATION));
        assertDoesNotThrow(() -> user1.validatePermissions(1, ManagerPermissions.CUSTOMER_SUPPORT));
        assertDoesNotThrow(() -> user1.validatePermissions(1, ManagerPermissions.VIEW_PURCHASE_HISTORY));
        assertDoesNotThrow(() -> user1.validatePermissions(1, ManagerPermissions.SALES_REPORT));
    }
}
