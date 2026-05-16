package com.group16b.DomainLayer.ProductionCompany;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.group16b.DomainLayer.Policies.DiscountPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;
import com.group16b.DomainLayer.ProductionCompany.membership.HierarchyNodeData;
import com.group16b.DomainLayer.ProductionCompany.membership.MembershipNode;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.User.Roles.ManagerPermissions;
import com.group16b.DomainLayer.User.Roles.RoleType;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductionCompanyTests {

    private ProductionCompany company;

    @BeforeEach
    void setUp() {
        company = new ProductionCompany(1, "Company", 5.0,1);

    }

    // ---------- Assign Owner ----------

    @Test
    void GivenOwnerCaller_WhenAssignOwner_ThenInviteCanBeAccepted() {
        company.AssignOwner(1, 2);

        assertDoesNotThrow(() -> company.acceptInvite(2, 1));
    }

    @Test
    void GivenNonOwnerCaller_WhenAssignOwner_ThenThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> company.AssignOwner(2, 3)
        );
    }

    @Test
    void GivenExistingOwner_WhenAssignOwner_ThenThrowException() {
        company.AssignOwner(1, 2);
        company.acceptInvite(2, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> company.AssignOwner(1, 2)
        );
    }

    // ---------- Assign Manager ----------

    @Test
    void GivenOwnerCaller_WhenAssignManager_ThenInviteCanBeAccepted() {
        Set<ManagerPermissions> perms =
                Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

        company.AssignManager(1, 2, perms);

        assertDoesNotThrow(() -> company.acceptInvite(2, 1));
    }

    @Test
    void GivenNonOwnerCaller_WhenAssignManager_ThenThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> company.AssignManager(
                        2,
                        3,
                        Set.of(ManagerPermissions.CUSTOMER_SUPPORT)
                )
        );
    }

    @Test
    void GivenExistingManager_WhenAssignManager_ThenThrowException() {
        company.AssignManager(
                1,
                2,
                Set.of(ManagerPermissions.CUSTOMER_SUPPORT)
        );

        company.acceptInvite(2, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> company.AssignManager(
                        1,
                        2,
                        Set.of(ManagerPermissions.CUSTOMER_SUPPORT)
                )
        );
    }

    // ---------- Accept Invite ----------

    @Test
    void GivenExistingInvite_WhenAcceptInvite_ThenMemberAddedToHierarchy() {
        company.AssignOwner(1, 2);

        company.acceptInvite(2, 1);

        List<HierarchyNodeData> hierarchy = company.getHierarchyTree(1);

        assertTrue(
                hierarchy.stream()
                        .anyMatch(n -> n.getUserID() == 2)
        );
    }

    @Test
    void GivenMissingInvite_WhenAcceptInvite_ThenThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> company.acceptInvite(2, 1)
        );
    }

    // ---------- Reject Invite ----------

    @Test
    void GivenExistingInvite_WhenRejectInvite_ThenInviteRemoved() {
        company.AssignOwner(1, 2);

        company.rejectInvite(2, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> company.acceptInvite(2, 1)
        );
    }

    @Test
    void GivenMissingInvite_WhenRejectInvite_ThenThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> company.rejectInvite(2, 1)
        );
    }

    // ---------- Forfeit Ownership ----------

    @Test
    void GivenOwner_WhenForfeitOwnership_ThenRemovedFromHierarchy() {
        company.AssignOwner(1, 2);
        company.acceptInvite(2, 1);

        company.forfeitOwnership(2);

        List<HierarchyNodeData> hierarchy = company.getHierarchyTree(1);

        assertFalse(
                hierarchy.stream()
                        .anyMatch(n -> n.getUserID() == 2)
        );
    }

    @Test
    void GivenNonOwner_WhenForfeitOwnership_ThenThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> company.forfeitOwnership(2)
        );
    }

    // ---------- Remove Member ----------

    @Test
    void GivenManagedUser_WhenRemoveMemberByOwner_ThenUserRemoved() {
        company.AssignOwner(1, 2);
        company.acceptInvite(2, 1);

        company.removeMemberByOwner(1, 2);

        List<HierarchyNodeData> hierarchy = company.getHierarchyTree(1);

        assertFalse(
                hierarchy.stream()
                        .anyMatch(n -> n.getUserID() == 2)
        );
    }

    @Test
    void GivenUnmanagedUser_WhenRemoveMemberByOwner_ThenThrowException() {
        company.AssignOwner(1, 2);
        company.acceptInvite(2, 1);

        company.AssignOwner(2, 3);
        company.acceptInvite(3, 2);

        assertThrows(
                IllegalArgumentException.class,
                () -> company.removeMemberByOwner(3, 2)
        );
    }

    // ---------- Update Permissions ----------

    @Test
    void GivenManagedManager_WhenUpdatePermissionsOfManager_ThenPermissionsUpdated() {
        Set<ManagerPermissions> oldPerms =
                Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

        Set<ManagerPermissions> newPerms =
                Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

        company.AssignManager(1, 2, oldPerms);
        company.acceptInvite(2, 1);

        company.updatePermissionsOfManager(1, 2, newPerms);

        List<HierarchyNodeData> hierarchy = company.getHierarchyTree(1);

        HierarchyNodeData node = hierarchy.stream()
                .filter(n -> n.getUserID() == 2)
                .findFirst()
                .orElseThrow();

        assertEquals(newPerms, node.getPermissions());
    }

    @Test
    void GivenUnmanagedManager_WhenUpdatePermissionsOfManager_ThenThrowException() {
        company.AssignOwner(1, 2);
        company.acceptInvite(2, 1);

        company.AssignManager(
                2,
                3,
                Set.of(ManagerPermissions.CUSTOMER_SUPPORT)
        );

        company.acceptInvite(3, 2);

        assertThrows(
                IllegalArgumentException.class,
                () -> company.updatePermissionsOfManager(
                        3,
                        2,
                        Set.of(ManagerPermissions.CUSTOMER_SUPPORT)
                )
        );
    }

    // ---------- Hierarchy ----------

    @Test
    void GivenOwnerRequester_WhenGetHierarchyTree_ThenReturnHierarchy() {
        company.AssignOwner(1, 2);
        company.acceptInvite(2, 1);

        List<HierarchyNodeData> hierarchy =
                company.getHierarchyTree(1);

        assertEquals(2, hierarchy.size());
    }

    @Test
    void GivenNonOwnerRequester_WhenGetHierarchyTree_ThenThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> company.getHierarchyTree(2)
        );
    }

    // ---------- Reparenting ----------

    @Test
    void GivenHierarchy_WhenRemoveMiddleOwner_ThenChildrenReparented() {
        company.AssignOwner(1, 2);
        company.acceptInvite(2, 1);

        company.AssignManager(
                2,
                3,
                Set.of(ManagerPermissions.CUSTOMER_SUPPORT)
        );

        company.acceptInvite(3, 2);

        company.removeMemberByOwner(1, 2);

        List<HierarchyNodeData> hierarchy =
                company.getHierarchyTree(1);

        HierarchyNodeData child = hierarchy.stream()
                .filter(n -> n.getUserID() == 3)
                .findFirst()
                .orElseThrow();

        assertEquals(1, child.getParentID());
    }
}