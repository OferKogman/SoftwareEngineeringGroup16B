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
    private final String childFreeOwnerID = "7";
    private final String depthCheckOwnerID = "8";
    private final String depthCheckManagerID = "9";

    private final ManagerPermissions managerPerm = ManagerPermissions.CUSTOMER_SUPPORT;
    private final ManagerPermissions newManagerPerm = ManagerPermissions.EVENT_INVENTORY;

    private final Set<String> defaultOwnerChildren = Set.of(managerID,depthCheckOwnerID);
    private final Set<String> defaultFounderChildren = Set.of(ownerID, childFreeOwnerID);

    @BeforeEach
    void setUp() {
        company = new ProductionCompany(1, "Company", 5.0,"1");
        company.AssignOwner(founderID, ownerID);
        company.acceptInvite(ownerID, founderID);

        company.AssignOwner(founderID, childFreeOwnerID);
        company.acceptInvite(childFreeOwnerID, founderID);

        company.AssignOwner(ownerID, depthCheckOwnerID);
        company.acceptInvite(depthCheckOwnerID, ownerID);
        company.AssignManager(depthCheckOwnerID, depthCheckManagerID, Set.of(managerPerm));
        company.acceptInvite(depthCheckManagerID, depthCheckOwnerID);

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
        assertFalse(company.isManager(nonMemberID));
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
    void GivenOwnerInvites_whenAssignOwnerFromSameOwner_thenOverrideInvite() {
        company.AssignOwner(ownerID, invitedManagerID);
        assertTrue(company.hasPendingOwnerInvite(invitedManagerID, ownerID));
        assertFalse(company.hasPendingManagerInvite(invitedManagerID, ownerID, Set.of(managerPerm)));

        company.AssignOwner(ownerID, invitedOwnerID);
        assertTrue(company.hasPendingOwnerInvite(invitedOwnerID, ownerID));
    }

    @Test
    void givenOwnerInvites_whenAssignOwnerFromOtherOwner_thenAddInviteToTheirList() {
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
        assertFalse(company.isOwner(nonMemberID));
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

    //i know that for now when owner is a manager, its covered by the prev tests, but if we change that in the future, we should add a test for it
    @Test
    void GivenOwner_WhenAssignExistingOwnerOrFounder_ThenThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> company.AssignManager(
                        founderID,
                        ownerID,
                        Set.of(ManagerPermissions.CUSTOMER_SUPPORT)
                )
        );
        assertFalse(company.hasPendingInvite(ownerID));

        assertThrows(
                IllegalArgumentException.class,
                () -> company.AssignManager(
                        ownerID,
                        founderID,
                        Set.of(ManagerPermissions.CUSTOMER_SUPPORT)
                )
        );
        assertFalse(company.hasPendingInvite(founderID));
    }

    @Test
    void GivenNullPermissions_WhenAssignManager_ThenThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> company.AssignManager(
                        ownerID,
                        nonMemberID,
                        null
                )
        );
        assertFalse(company.hasPendingInvite(nonMemberID));
        assertFalse(company.isManager(nonMemberID));
        assertFalse(company.isOwner(nonMemberID));
    }
    @Test
    void GivenEmptyPermissions_WhenAssignManager_ThenThrowException() {
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
        assertFalse(company.isOwner(nonMemberID));
    }

    @Test
    void GivenOwnerInvites_WhenAssignManagerFromAnotherOwner_ThenManagerInviteAdded() {
        company.AssignManager(founderID,invitedOwnerID,Set.of(newManagerPerm));
        company.AssignManager(founderID,invitedManagerID,Set.of(newManagerPerm));

        assertTrue(company.hasPendingOwnerInvite(invitedOwnerID,ownerID));
        assertTrue(company.hasPendingManagerInvite(invitedManagerID,ownerID,Set.of(managerPerm)));
        
        assertTrue(company.hasPendingManagerInvite(invitedManagerID,founderID,Set.of(newManagerPerm)));
        assertTrue(company.hasPendingManagerInvite(invitedOwnerID,founderID,Set.of(newManagerPerm)));
    }

    @Test
    void GivenOwnerInvites_WhenAssignManagerFromSameOwner_ThenOverrideInvite() {
        company.AssignManager(ownerID, invitedManagerID, Set.of(newManagerPerm));
        assertTrue(company.hasPendingManagerInvite(invitedManagerID, ownerID, Set.of(newManagerPerm)));

        company.AssignManager(ownerID, invitedOwnerID,  Set.of(newManagerPerm));
        assertTrue(company.hasPendingManagerInvite(invitedManagerID, ownerID, Set.of(newManagerPerm)));
        
        assertFalse(company.hasPendingOwnerInvite(invitedManagerID, ownerID));
        assertFalse(company.hasPendingOwnerInvite(invitedOwnerID, ownerID));
    }

    // ---------- Accept Invite ----------
    @Test
    void GivenExistingOwnerAndManagerInvites_WhenAcceptManagerInvite_ThenOwnerInviteSavedAndManagerInviteRemoved()
    {
        company.AssignOwner(founderID, invitedManagerID);
        company.AssignManager(childFreeOwnerID, invitedManagerID, Set.of(newManagerPerm));

        assertTrue(company.hasPendingOwnerInvite(invitedManagerID, founderID));
        assertTrue(company.hasPendingManagerInvite(invitedManagerID, childFreeOwnerID, Set.of(newManagerPerm)));

        company.acceptInvite(invitedManagerID, ownerID);

        assertTrue(company.isManager(invitedManagerID));
        assertFalse(company.isOwner(invitedManagerID));

        assertTrue(company.hasPendingOwnerInvite(invitedManagerID, founderID));
        assertFalse(company.hasPendingManagerInvite(invitedManagerID, ownerID, Set.of(managerPerm)));
        assertFalse(company.hasPendingManagerInvite(invitedManagerID, childFreeOwnerID, Set.of(newManagerPerm)));

        assertTrue(company.isDirectSubordinate(ownerID, invitedManagerID));
        assertFalse(company.isDirectSubordinate(childFreeOwnerID, invitedManagerID));
        assertFalse(company.isDirectSubordinate(founderID, invitedManagerID));
        
    }

    @Test
    void givenExistingInvites_WhenAcceptOwnerInvite_ThenAllInvitesAreRemoved() {
        company.AssignOwner(founderID, invitedOwnerID);
        company.AssignManager(childFreeOwnerID, invitedOwnerID, Set.of(managerPerm));
        company.acceptInvite(invitedOwnerID, ownerID);

        assertTrue(company.isOwner(invitedOwnerID));
        assertFalse(company.hasPendingInvite(invitedOwnerID));

        assertTrue(company.isDirectSubordinate(ownerID, invitedOwnerID));
        assertFalse(company.isDirectSubordinate(childFreeOwnerID, invitedOwnerID));
        assertFalse(company.isDirectSubordinate(founderID, invitedOwnerID));
    }

    @Test
    void GivenMissingInvite_WhenAcceptInvite_ThenThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> company.acceptInvite(nonMemberID, nonMemberID)
        );
    }

    @Test
    void GivenOwnerInvite_WhenAcceptInviteByManager_ThenPromote() {
        assertTrue(company.isManager(managerID));
        assertFalse(company.isOwner(managerID));
        assertTrue(company.isDirectSubordinate(ownerID, managerID));
        assertFalse(company.isDirectSubordinate(childFreeOwnerID, managerID));
        assertFalse(company.isDirectSubordinate(founderID, managerID));

        company.AssignOwner(founderID, managerID);
        company.acceptInvite(managerID, founderID);
        assertTrue(company.isOwner(managerID));
        assertFalse(company.isFounder(managerID));
        assertFalse(company.hasPendingInvite(managerID));

        assertFalse(company.isDirectSubordinate(ownerID, managerID));
        assertFalse(company.isDirectSubordinate(childFreeOwnerID, managerID));
        assertTrue(company.isDirectSubordinate(founderID, managerID));
    }

    // ---------- Reject Invite ----------
    @Test
    void GivenExistingInvite_WhenRejectInvite_ThenOnlyThatInviteRemoved() {
        company.AssignOwner(founderID, invitedOwnerID);
        assertTrue(company.hasPendingOwnerInvite(invitedOwnerID, founderID));
        assertTrue(company.hasPendingOwnerInvite(invitedOwnerID, ownerID));

        company.rejectInvite(invitedOwnerID, ownerID);

        //ensure only the invite from ownerID is removed, and the one from founderID is still there
        //and ensure other members are not affected by the rejection
        //and ensure no hierarchy changes were made since the invite was rejected and not accepted
        assertFalse(company.hasPendingOwnerInvite(invitedOwnerID, ownerID));
        assertTrue(company.hasPendingOwnerInvite(invitedOwnerID, founderID));

        assertFalse(company.isOwner(invitedOwnerID));
        assertFalse(company.isManager(invitedOwnerID));

        assertFalse(company.isDirectSubordinate(ownerID, invitedOwnerID));
        assertFalse(company.isDirectSubordinate(childFreeOwnerID, invitedOwnerID));
        assertFalse(company.isDirectSubordinate(founderID, invitedOwnerID));

        assertTrue(company.hasPendingManagerInvite(invitedManagerID, ownerID, Set.of(managerPerm)));

    }

    @Test
    void GivenMissingInvite_WhenRejectInvite_ThenThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> company.rejectInvite(nonMemberID, nonMemberID)
        );
    }

    // ---------- Forfeit Ownership ----------

    @Test
    void GivenOwnerWithChildrenAndInvites_WhenForfeitOwnership_ThenRemovedFromHierarchy() {
        company.forfeitOwnership(ownerID);

        assertFalse(company.isOwner(ownerID));
        assertFalse(company.isManager(ownerID));

        assertFalse(company.hasOutgoingInvites(ownerID));

        assertFalse(company.isDirectSubordinate(founderID, ownerID));
        assertTrue(company.areDirectSubordinates(founderID, defaultOwnerChildren));
        assertTrue(company.isDirectSubordinate(founderID, childFreeOwnerID));

        assertFalse(company.areDirectSubordinates(ownerID, defaultOwnerChildren));

        assertTrue(company.isDirectSubordinate(depthCheckOwnerID, depthCheckManagerID));

        assertFalse(company.hasPendingInvite(ownerID));
    }

    @Test
    void GivenFounder_WhenForfeitOwnership_ThenThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> company.forfeitOwnership(founderID)
        );
        assertTrue(company.isFounder(founderID));
        assertTrue(company.areDirectSubordinates(founderID, defaultFounderChildren));
    }

    @Test
    void GivenOwnerWithoutChildren_WhenForfeitOwnership_ThenRemovedFromHierarchy() {
        company.forfeitOwnership(childFreeOwnerID);

        assertFalse(company.isOwner(childFreeOwnerID));
        assertFalse(company.isManager(childFreeOwnerID));

        assertFalse(company.hasOutgoingInvites(childFreeOwnerID));

        assertFalse(company.isDirectSubordinate(founderID, childFreeOwnerID));
        assertTrue(company.areDirectSubordinates(founderID, Set.of(ownerID)));

        assertTrue(company.areDirectSubordinates(ownerID, defaultOwnerChildren));

        assertFalse(company.hasPendingInvite(childFreeOwnerID));
    }

    @Test
    void GivenNonOwner_WhenForfeitOwnership_ThenThrowException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> company.forfeitOwnership(managerID)
        );
        assertTrue(company.isManager(managerID));
        assertFalse(company.isOwner(managerID));

        assertThrows(
                IllegalArgumentException.class,
                () -> company.forfeitOwnership(nonMemberID)
        );
        assertFalse(company.isManager(nonMemberID));
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