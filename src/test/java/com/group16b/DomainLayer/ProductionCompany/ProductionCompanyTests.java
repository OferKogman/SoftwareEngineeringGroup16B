package com.group16b.DomainLayer.ProductionCompany;


import java.util.List;
import java.util.Set;

import com.group16b.DomainLayer.ProductionCompany.membership.HierarchyNodeData;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.ProductionCompany.membership.RoleType;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductionCompanyTests {

    private ProductionCompany company;
    private final String founderID = "1";
    private final String ownerID = "2";
    private final String managerID = "3";
    private final String invitedManagerID = "4";
    private final String invitedOwnerID = "5";
    private final String nonMemberID = "6";

    private final ManagerPermissions managerPerm = ManagerPermissions.CUSTOMER_SUPPORT;
    

    @BeforeEach
    void setUp() {
        company = new ProductionCompany(1, "Company", 5.0,"1");
        company.AssignOwner(founderID, ownerID);
        company.acceptInvite(ownerID, founderID);

        company.AssignManager(ownerID, managerID, Set.of(managerPerm));
        company.acceptInvite(managerID, ownerID);

        company.AssignOwner(ownerID, invitedOwnerID);
        company.AssignManager(ownerID, invitedManagerID, Set.of(managerPerm));


    }

    // ---------- Assign Owner ----------

    @Test
    void GivenOwnerCaller_WhenAssignOwner_ThenInviteIsCreated() {
        company.AssignOwner(ownerID, nonMemberID);

        assertFalse(company.isOwner(nonMemberID));
        assertTrue(company.hasPendingOwnerInvite(nonMemberID, ownerID));
    }

    @Test
    void GivenNonOwnerCaller_WhenAssignOwner_ThenThrowException() {
        assertThrows(IllegalArgumentException.class,() -> company.AssignOwner(managerID, nonMemberID));
        assertThrows(IllegalArgumentException.class,() -> company.AssignOwner(nonMemberID, nonMemberID));
        assertFalse(company.hasPendingInvite(nonMemberID));
    }

    @Test
    void GivenOwner_WhenAssignExistingOwner_ThenThrowException() {
        assertThrows(IllegalArgumentException.class,() -> company.AssignOwner(founderID, ownerID));
        assertFalse(company.hasPendingInvite(ownerID));
    }

    @Test
    void GivenOwner_WhenAssignExistingManager_thenInviteCreated() {
        assertTrue(company.isManager(managerID));
        assertFalse(company.isOwner(managerID));

        company.AssignOwner(ownerID, managerID);
        assertTrue(company.hasPendingOwnerInvite(managerID, ownerID));
        assertFalse(company.isOwner(managerID));
        assertTrue(company.isManager(managerID));
    }

    @Test
    void GivenOwner_whenAssignUsersWithExistingInviteFromSameOwner_thenOverrideInvite() {
        company.AssignOwner(ownerID, invitedManagerID);
        assertTrue(company.hasPendingOwnerInvite(invitedManagerID, ownerID));

        company.AssignOwner(ownerID, invitedOwnerID);
        assertTrue(company.hasPendingOwnerInvite(invitedOwnerID, ownerID));
    }

    @Test
    void givenFounder_whenAssignOwnerForUsersWithExistingInvitesFromOtherOwners_thenAddInviteToTheirList() {
        company.AssignOwner(founderID, invitedManagerID);
        company.AssignOwner(founderID, invitedOwnerID);

        assertTrue(company.hasPendingManagerInvite(invitedManagerID, ownerID,Set.of(managerPerm)));
        assertTrue(company.hasPendingOwnerInvite(invitedManagerID, founderID));
        assertTrue(company.hasPendingOwnerInvite(invitedOwnerID, ownerID));
        assertTrue(company.hasPendingOwnerInvite(invitedOwnerID, founderID));
    }

    @Test
    void givenOwner_whenAssignOwnerToFounder_thenThrowException() {
        assertThrows(IllegalArgumentException.class, () -> company.AssignOwner(ownerID, founderID));
    }

    // ---------- Assign Manager ----------

    @Test
    void GivenOwnerCaller_WhenAssignManager_ThenInviteIsCreated() {
        company.AssignManager(ownerID,nonMemberID, Set.of(managerPerm));

        assertFalse(company.isManager(nonMemberID));
        assertTrue(company.hasPendingManagerInvite(nonMemberID, ownerID,Set.of(managerPerm)));
        assertFalse(company.hasPendingOwnerInvite(nonMemberID, ownerID));
    }

    @Test
    void GivenNonOwnerCaller_WhenAssignManager_ThenThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> company.AssignManager(
                        managerID,
                        nonMemberID,
                        Set.of(ManagerPermissions.CUSTOMER_SUPPORT)
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> company.AssignManager(
                        nonMemberID,
                        nonMemberID,
                        Set.of(ManagerPermissions.CUSTOMER_SUPPORT)
                )
        );
        assertFalse(company.hasPendingInvite(nonMemberID));
    }

    @Test
    void GivenOwner_WhenAssignExistingManager_ThenThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> company.AssignManager(
                        ownerID,
                        managerID,
                        Set.of(ManagerPermissions.CUSTOMER_SUPPORT)
                )
        );
        assertFalse(company.hasPendingInvite(managerID));
    }

    @Test
    void GivenNullOrEmptyPermissions_WhenAssignManager_ThenThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> company.AssignManager(
                        ownerID,
                        nonMemberID,
                        null
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> company.AssignManager(
                        ownerID,
                        nonMemberID,
                        Set.of()
                )
        );
        assertFalse(company.hasPendingInvite(nonMemberID));
        assertFalse(company.isManager(nonMemberID));
    }

    // ---------- Accept Invite ----------

    @Test
    void GivenExistingInvite_WhenAcceptInvite_ThenMemberAddedToHierarchy() {
        company.acceptInvite(invitedOwnerID, ownerID);
        assertTrue(company.isOwner(invitedOwnerID));

        company.acceptInvite(invitedManagerID, ownerID);
        assertTrue(company.isManager(invitedManagerID));

        assertFalse(company.isOwner(invitedManagerID));
        assertFalse(company.hasPendingInvite(invitedOwnerID));
        assertFalse(company.hasPendingInvite(invitedManagerID));
    }

    @Test
    void GivenMissingInvite_WhenAcceptInvite_ThenThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> company.acceptInvite("2", "1")
        );
    }

    // ---------- Reject Invite ----------

    @Test
    void GivenExistingInvite_WhenRejectInvite_ThenInviteRemoved() {
        company.AssignOwner("1", "2");

        company.rejectInvite("2", "1");

        assertThrows(
                IllegalArgumentException.class,
                () -> company.acceptInvite("2", "1")
        );
    }

    @Test
    void GivenMissingInvite_WhenRejectInvite_ThenThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> company.rejectInvite("2", "1")
        );
    }

    // ---------- Forfeit Ownership ----------

    @Test
    void GivenOwner_WhenForfeitOwnership_ThenRemovedFromHierarchy() {
        company.AssignOwner("1", "2");
        company.acceptInvite("2", "1");

        company.forfeitOwnership("2");

        List<HierarchyNodeData> hierarchy = company.getHierarchyTree("1");

        assertFalse(
                hierarchy.stream()
                        .anyMatch(n -> n.getUserID().equals("2"))
        );
    }

    @Test
    void GivenNonOwner_WhenForfeitOwnership_ThenThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> company.forfeitOwnership("2")
        );
    }

    // ---------- Remove Member ----------

    @Test
    void GivenManagedUser_WhenRemoveMemberByOwner_ThenUserRemoved() {
        company.AssignOwner("1", "2");
        company.acceptInvite("2", "1");

        company.removeMemberByOwner("1", "2");

        List<HierarchyNodeData> hierarchy = company.getHierarchyTree("1");

        assertFalse(
                hierarchy.stream()
                        .anyMatch(n -> n.getUserID().equals("2"))
        );
    }

    @Test
    void GivenUnmanagedUser_WhenRemoveMemberByOwner_ThenThrowException() {
        company.AssignOwner("1", "2");
        company.acceptInvite("2", "1");

        company.AssignOwner("2", "3");
        company.acceptInvite("3", "2");

        assertThrows(
                IllegalArgumentException.class,
                () -> company.removeMemberByOwner("3", "2")
        );
    }

    // ---------- Update Permissions ----------

    @Test
    void GivenManagedManager_WhenUpdatePermissionsOfManager_ThenPermissionsUpdated() {
        Set<ManagerPermissions> oldPerms =
                Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

        Set<ManagerPermissions> newPerms =
                Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

        company.AssignManager("1", "2", oldPerms);
        company.acceptInvite("2", "1");

        company.updatePermissionsOfManager("1", "2", newPerms);

        List<HierarchyNodeData> hierarchy = company.getHierarchyTree("1");

        HierarchyNodeData node = hierarchy.stream()
                .filter(n -> n.getUserID().equals("2"))
                .findFirst()
                .orElseThrow();

        assertEquals(newPerms, node.getPermissions());
    }

    @Test
    void GivenUnmanagedManager_WhenUpdatePermissionsOfManager_ThenThrowException() {
        company.AssignOwner("1", "2");
        company.acceptInvite("2", "1");

        company.AssignManager(
                "2",
                "3",
                Set.of(ManagerPermissions.CUSTOMER_SUPPORT)
        );

        company.acceptInvite("3", "2");

        assertThrows(
                IllegalArgumentException.class,
                () -> company.updatePermissionsOfManager(
                        "3",
                        "2",
                        Set.of(ManagerPermissions.CUSTOMER_SUPPORT)
                )
        );
    }

    // ---------- Hierarchy ----------

    @Test
    void GivenOwnerRequester_WhenGetHierarchyTree_ThenReturnHierarchy() {
        company.AssignOwner("1", "2");
        company.acceptInvite("2", "1");

        List<HierarchyNodeData> hierarchy =
                company.getHierarchyTree("1");

        assertEquals(2, hierarchy.size());
    }

    @Test
    void GivenNonOwnerRequester_WhenGetHierarchyTree_ThenThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> company.getHierarchyTree("2")
        );
    }

    // ---------- Reparenting ----------

    @Test
    void GivenHierarchy_WhenRemoveMiddleOwner_ThenChildrenReparented() {
        company.AssignOwner("1", "2");
        company.acceptInvite("2", "1");

        company.AssignManager(
                "2",
                "3",
                Set.of(ManagerPermissions.CUSTOMER_SUPPORT)
        );

        company.acceptInvite("3", "2");

        company.removeMemberByOwner("1", "2");

        List<HierarchyNodeData> hierarchy =
                company.getHierarchyTree("1");

        HierarchyNodeData child = hierarchy.stream()
                .filter(n -> n.getUserID().equals("3"))
                .findFirst()
                .orElseThrow();

        assertEquals("1", child.getParentID());
    }

    //----------- roleship tests ----------
    @Test
    void founder_IsFounderAndOwnerAndManager() {
        assertTrue(company.isFounder("1"));
        assertTrue(company.isOwner("1"));
        assertTrue(company.isManager("1"));
    }

    @Test
    void manager_IsManagerButNotOwner() {
        company.AssignManager("1", "2",
                Set.of(ManagerPermissions.CUSTOMER_SUPPORT));
        company.acceptInvite("2", "1");

        assertTrue(company.isManager("2"));
        assertFalse(company.isOwner("2"));
        assertThrows(IllegalArgumentException.class, () -> company.validateUserPermissions("2", RoleType.OWNER));
    }

    @Test
    void founderCannotForfeitOwnership() {
        assertThrows(
                IllegalArgumentException.class,
                () -> company.forfeitOwnership("1")
        );
    }

    @Test
    void managerWithPermission_PassesValidation() {
        company.AssignManager("1", "2",
                Set.of(ManagerPermissions.CUSTOMER_SUPPORT));

        company.acceptInvite("2", "1");

        assertDoesNotThrow(
                () -> company.validateUserPermissions(
                "2",
                ManagerPermissions.CUSTOMER_SUPPORT
                )
        );
    }
    @Test
    void managerWithoutPermission_FailsValidation() {
        company.AssignManager("1", "2",
                Set.of(ManagerPermissions.CUSTOMER_SUPPORT));

        company.acceptInvite("2", "1");

        assertThrows(
                IllegalArgumentException.class,
                () -> company.validateUserPermissions(
                        "2",
                        ManagerPermissions.EVENT_INVENTORY
                )
        );
    }
    @Test
    void ownerCanPassManagerValidation() {
        company.AssignOwner("1", "2");
        company.acceptInvite("2", "1");

        assertDoesNotThrow(
                () -> company.validateUserPermissions(
                "2",
                RoleType.MANAGER
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> company.validateUserPermissions(
                        "2",
                        RoleType.FOUNDER
                )
        );
        assertDoesNotThrow(
                () -> company.validateUserPermissions(
                        "2",
                        RoleType.OWNER
                )
        );
        assertTrue(company.isManager("2"));
        assertFalse(company.isFounder("2"));
        assertTrue(company.isOwner("2"));
    }

    @Test
    void ownerCanPassOwnerValidation() {
        company.AssignOwner("1", "2");
        company.acceptInvite("2", "1");

        assertDoesNotThrow(
                () -> company.validateUserPermissions(
                "2",
                RoleType.OWNER
                )
        );
        assertDoesNotThrow(
                () -> company.validateUserPermissions(
                "2",
                RoleType.MANAGER
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> company.validateUserPermissions(
                        "2",
                        RoleType.FOUNDER
                )
        );
        assertTrue(company.isOwner("2"));
        assertTrue(company.isManager("2"));
        assertFalse(company.isFounder("2"));
    }

    // ---------- invites tests ----------
    @Test
    void cannotAcceptInviteForAnotherUser() {
                company.AssignOwner("1", "2");
        
                assertThrows(
                        IllegalArgumentException.class,
                        () -> company.acceptInvite("3", "1")
                );
    }

    @Test
    void cannotRejectInviteForAnotherUser() {
        company.AssignOwner("1", "2");

        assertThrows(
                IllegalArgumentException.class,
                () -> company.rejectInvite("3", "1")
        );
    }

    @Test
    void rejectingInviteRemovesItFromPendingInvites() {
        company.AssignOwner("1", "2");

        company.rejectInvite("2", "1");

        assertThrows(
                IllegalArgumentException.class,
                () -> company.acceptInvite("2", "1")
        );
        assertFalse(
                company.hasPendingInvite("2")
        );
    }

}