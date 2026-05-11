package com.group16b.ServiceLayer;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.CompanyHierarchyService;
import com.group16b.DomainLayer.DomainServices.CompanyHierarchyDomainService;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.Roles.Manager;
import com.group16b.DomainLayer.User.Roles.Owner;
import com.group16b.DomainLayer.User.User;
public class CompanyHierarchyServiceTests {
    CompanyHierarchyService userService;
    IAuthenticationService mockAuthService;
    IUserRepository mockUserRepository;
    CompanyHierarchyDomainService mockCompanyHierarchyDomainService;

    @BeforeEach
    void setUp() {
        mockAuthService = mock(IAuthenticationService.class);
        mockUserRepository = mock(IUserRepository.class);
        mockCompanyHierarchyDomainService=mock(CompanyHierarchyDomainService.class);
        userService = new CompanyHierarchyService(mockAuthService, mockUserRepository,mockCompanyHierarchyDomainService);
    }

    //good
    @Test
    void testAssignOwnerToCompanySuccess() {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;
        User mockUser = mock(User.class);
        User mockTarget=mock(User.class);
        doNothing().when(mockUser).validatePermissions(anyInt(), eq(Owner.class));

        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);
        when(mockUserRepository.userExists(targetID)).thenReturn(true);
        when(mockTarget.getUserInvitesLock()).thenReturn(new ReentrantLock());

        assertTrue(userService.assignOwnerToCompany(companyID, targetID, "").isSuccess());
    }

    //user not owner
    @Test
    void testAssignOwnerToCompanyByNonOwnerFail() {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;
        User mockUser = mock(User.class);
        User mockTarget=mock(User.class);
        doThrow(new RuntimeException("User is not an owner")).when(mockUser).validatePermissions(anyInt(), eq(Owner.class));

        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);
        when(mockUserRepository.userExists(targetID)).thenReturn(true);
        when(mockTarget.getUserInvitesLock()).thenReturn(new ReentrantLock());

        assertFalse(userService.assignOwnerToCompany(companyID, targetID, "").isSuccess());
    }

    //target user not found
    @Test
    void testAssignOwnerToCompanyNonExistingTargetFail() {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;
        User mockUser = mock(User.class);
        User mockTarget=mock(User.class);
        doNothing().when(mockUser).validatePermissions(anyInt(), eq(Owner.class));

        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(null);
        when(mockUserRepository.userExists(targetID)).thenReturn(false);
        when(mockTarget.getUserInvitesLock()).thenReturn(new ReentrantLock());

        assertFalse(userService.assignOwnerToCompany( companyID, targetID, "").isSuccess());
    }

    //target user already owner
    @Test
    void testAssignOwnerToCompanyTargetAlreadyOwnerFail() {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;
        User mockUser = mock(User.class);
        User mockTarget=mock(User.class);
        doNothing().when(mockUser).validatePermissions(anyInt(), eq(Owner.class));
        doThrow(new IllegalArgumentException("User already has a role for this company")).when(mockTarget).addInvite(anyInt(),anyInt(), any(Owner.class));
        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);
        when(mockUserRepository.userExists(targetID)).thenReturn(true);
        when(mockTarget.getUserInvitesLock()).thenReturn(new ReentrantLock());

        assertFalse(userService.assignOwnerToCompany(companyID, targetID, "").isSuccess());
    }


    @Test
    void testAssignOwnerToCompanyInvalidSessionFail() {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;

        when(mockAuthService.validateToken(anyString())).thenReturn(false);

        assertFalse(userService.assignOwnerToCompany(companyID, targetID, "").isSuccess());
    }

    
    //---------------------------------------------------------------------
    //    ADD MANAGER ASSIGMENT TESTS
    //----------------------------------------------------------------------

     @Test
    void testAssignManagerToCompanySuccess() {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;
        User mockUser = mock(User.class);
        User mockTarget=mock(User.class);
        doNothing().when(mockUser).validatePermissions(anyInt(), eq(Owner.class));

        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);
        when(mockUserRepository.userExists(targetID)).thenReturn(true);
        when(mockTarget.getUserInvitesLock()).thenReturn(new ReentrantLock());

        assertTrue(userService.assignManagerToCompany(companyID, targetID, Collections.emptySet(), "").isSuccess());
    }

    //user not owner
    @Test
    void testAssignManagerToCompanyByNonOwnerFail() {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;
        User mockUser = mock(User.class);
        User mockTarget=mock(User.class);
        doThrow(new RuntimeException("User is not an owner")).when(mockUser).validatePermissions(anyInt(), eq(Owner.class));

        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);
        when(mockUserRepository.userExists(targetID)).thenReturn(true);
        when(mockTarget.getUserInvitesLock()).thenReturn(new ReentrantLock());

        assertFalse(userService.assignManagerToCompany(companyID, targetID, Collections.emptySet(), "").isSuccess());
    }

    //target user not found
    @Test
    void testAssignManagerToCompanyNonExistingTargetFail() {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;
        User mockUser = mock(User.class);
        User mockTarget=mock(User.class);
        doNothing().when(mockUser).validatePermissions(anyInt(), eq(Owner.class));

        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(null);
        when(mockUserRepository.userExists(targetID)).thenReturn(false);
        when(mockTarget.getUserInvitesLock()).thenReturn(new ReentrantLock());

        assertFalse(userService.assignManagerToCompany(companyID, targetID, Collections.emptySet(), "").isSuccess());
    }

    //target user already owner
    @Test
    void testAssignManagerToCompanyTargetAlreadyManagerFail() {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;
        User mockUser = mock(User.class);
        User mockTarget=mock(User.class);
        doNothing().when(mockUser).validatePermissions(anyInt(), eq(Owner.class));
        doThrow(new IllegalArgumentException("User already has a role for this company")).when(mockTarget).addInvite(anyInt(),anyInt(), any(Manager.class));
        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);
        when(mockUserRepository.userExists(targetID)).thenReturn(true);
        when(mockTarget.getUserInvitesLock()).thenReturn(new ReentrantLock());

        assertFalse(userService.assignManagerToCompany(companyID, targetID, Collections.emptySet(), "").isSuccess());
    }


    @Test
    void testAssignManagerToCompanyInvalidSessionFail() {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;

        when(mockAuthService.validateToken(anyString())).thenReturn(false);

        assertFalse(userService.assignManagerToCompany(companyID, targetID, Collections.emptySet(), "").isSuccess());
    }




    //----------------------------------------------------------------------
    //    ACCEPT INVITE ASSIGMENT TESTS
    //----------------------------------------------------------------------

        @Test
    void testAcceptInviteAssignmentSuccess() {
        int userID = 1;
        int companyID = 1;
        int assignerID = 2;
        User mockUser = mock(User.class);
        User mockAssigner = mock(User.class);
        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.userExists(assignerID)).thenReturn(true);
        when(mockUserRepository.getUserByID(assignerID)).thenReturn(mockAssigner);
        when(mockUser.getUserInvitesLock()).thenReturn(new ReentrantLock());
        doNothing().when(mockAssigner).addAssignee(anyInt(), any(Manager.class));
        when(mockAssigner.isOwnerOfCompany(companyID)).thenReturn(true);
        assertTrue(userService.acceptInviteToCompany(companyID, assignerID, "").isSuccess());
    }

    @Test
    void testAcceptInviteAssignmentInvalidSessionFail() {
        int userID = 1;
        int companyID = 1;
        int assignerID = 2;

        when(mockAuthService.validateToken(anyString())).thenReturn(false);

        assertFalse(userService.acceptInviteToCompany( companyID, assignerID, "").isSuccess());
    }

    @Test
    void testAcceptInviteAssignmentNoInviteFail() {
        int userID = 1;
        int companyID = 1;
        int assignerID = 2;
        User mockUser = mock(User.class);
        doNothing().when(mockUser).validatePermissions(anyInt(), eq(Owner.class));
        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.userExists(assignerID)).thenReturn(true);
        when(mockUserRepository.getUserByID(assignerID)).thenReturn(mock(User.class));
        when(mockUser.getUserInvitesLock()).thenReturn(new ReentrantLock());
        doThrow(new IllegalArgumentException("No invite found")).when(mockUser).acceptInvite(companyID, assignerID);

        assertFalse(userService.acceptInviteToCompany( companyID, assignerID, "").isSuccess());
    }

    @Test
    void testAcceptInviteAssignmentAssignerNotFoundFail() {
        int userID = 1;
        int companyID = 1;
        int assignerID = 2;
        User mockUser = mock(User.class);
        doNothing().when(mockUser).validatePermissions(anyInt(), eq(Owner.class));
        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.userExists(assignerID)).thenReturn(false);

        assertFalse(userService.acceptInviteToCompany( companyID, assignerID, "").isSuccess());
    }


    //----------------------------------------------------------------------
    //    REJECT INVITE ASSIGMENT TESTS
    //----------------------------------------------------------------------

    @Test
    void testRejectAssignmentSuccess() {
        int userID = 1;
        int companyID = 1;
        int assignerID = 2;
        User mockUser = mock(User.class);
        doNothing().when(mockUser).validatePermissions(anyInt(), eq(Owner.class));
        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.userExists(assignerID)).thenReturn(true);
        doNothing().when(mockUser).rejectInvite(companyID, assignerID);
        when(mockUser.getUserInvitesLock()).thenReturn(new ReentrantLock());
        assertTrue(userService.rejectInviteToCompany( companyID, assignerID, "").isSuccess());
    }

    @Test
    void testRejectAssignmentNoInviteFailure() {
        int userID = 1;
        int companyID = 1;
        int assignerID = 2;
        User mockUser = mock(User.class);
        doNothing().when(mockUser).validatePermissions(anyInt(), eq(Owner.class));
        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.userExists(assignerID)).thenReturn(true);
        doThrow(new IllegalArgumentException("No invite found")).when(mockUser).rejectInvite(companyID, assignerID);
        when(mockUser.getUserInvitesLock()).thenReturn(new ReentrantLock());

        assertFalse(userService.rejectInviteToCompany( companyID, assignerID, "").isSuccess());
    }

    @Test
    void testRejectAssignmentBadSessionTokenFailure() {
        int userID = 1;
        int companyID = 1;
        int assignerID = 2;
        User mockUser = mock(User.class);
        doNothing().when(mockUser).validatePermissions(anyInt(), eq(Owner.class));
        when(mockAuthService.validateToken(anyString())).thenReturn(false);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.userExists(assignerID)).thenReturn(true);
        doNothing().when(mockUser).rejectInvite(companyID, assignerID);
        when(mockUser.getUserInvitesLock()).thenReturn(new ReentrantLock());
        assertFalse(userService.rejectInviteToCompany( companyID, assignerID, "").isSuccess());
    }

    //-----------------------------------------------------------------
    //   FORFEIT OWNERSHIP TESTS
    //-----------------------------------------------------------------
    @Test
    void testForfeitOwnershipSUccess() {
        int userID = 1;
        int companyID = 1;
        int assignerID = 2;
        User mockUser = mock(User.class);
        User mockAssigner=mock(User.class);
        Owner mockOwner=mock(Owner.class);
        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.userExists(assignerID)).thenReturn(true);
        when(mockUser.isOwnerOfCompany(companyID)).thenReturn(true);
        when(mockUser.getParentIDForCompany(companyID)).thenReturn(assignerID);
        when(mockUserRepository.getUserByID(assignerID)).thenReturn(mockAssigner);
        when(mockUser.getRole(companyID)).thenReturn(mockOwner);
        when(mockAssigner.getRole(companyID)).thenReturn(mockOwner);
       
        assertTrue(userService.forfeitOwnership( companyID, "").isSuccess());
    }

    @Test
    void testForfeitOwnershipUserNotOwnerFail() {
        int userID = 1;
        int companyID = 1;
        int assignerID = 2;
        User mockUser = mock(User.class);
        User mockAssigner=mock(User.class);
        Owner mockOwner=mock(Owner.class);
        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.userExists(assignerID)).thenReturn(true);
        when(mockUser.isOwnerOfCompany(companyID)).thenReturn(false);
        when(mockUser.getParentIDForCompany(companyID)).thenReturn(assignerID);
        when(mockUserRepository.getUserByID(assignerID)).thenReturn(mockAssigner);
        when(mockUser.getRole(companyID)).thenReturn(mockOwner);
        when(mockAssigner.getRole(companyID)).thenReturn(mockOwner);
       
        assertFalse(userService.forfeitOwnership( companyID, "").isSuccess());
    }

    @Test
    void testForfeitOwnershipUserIsDounderFail() {
        int userID = 1;
        int companyID = 1;
        int assignerID = 2;
        User mockUser = mock(User.class);
        User mockAssigner=mock(User.class);
        Owner mockOwner=mock(Owner.class);
        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.userExists(assignerID)).thenReturn(true);
        when(mockUser.isOwnerOfCompany(companyID)).thenReturn(true);
        when(mockUser.getParentIDForCompany(companyID)).thenReturn(null);
        when(mockUserRepository.getUserByID(assignerID)).thenReturn(mockAssigner);
        when(mockUser.getRole(companyID)).thenReturn(mockOwner);
        when(mockAssigner.getRole(companyID)).thenReturn(mockOwner);
       
        assertFalse(userService.forfeitOwnership( companyID, "").isSuccess());
    }

       @Test
    void testForfeitOwnershipAuthFail() {
        int userID = 1;
        int companyID = 1;
        int assignerID = 2;
        User mockUser = mock(User.class);
        User mockAssigner=mock(User.class);
        Owner mockOwner=mock(Owner.class);
        when(mockAuthService.validateToken(anyString())).thenReturn(false);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.userExists(assignerID)).thenReturn(true);
        when(mockUser.isOwnerOfCompany(companyID)).thenReturn(true);
        when(mockUser.getParentIDForCompany(companyID)).thenReturn(assignerID);
        when(mockUserRepository.getUserByID(assignerID)).thenReturn(mockAssigner);
        when(mockUser.getRole(companyID)).thenReturn(mockOwner);
        when(mockAssigner.getRole(companyID)).thenReturn(mockOwner);
       
        assertFalse(userService.forfeitOwnership( companyID, "").isSuccess());
    }

    //------------------------------------------------------------------------------------------
    //     REMOVE PERSONAL FROM COMPANY TESTS
    //------------------------------------------------------------------------------------------

    @Test
    void testRemoveManagerFromCompanySuccess()
    {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;
        User mockUser = mock(User.class);
        User mockTarget=mock(User.class);
        Manager mockRole=mock(Manager.class);
        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);
        when(mockUserRepository.userExists(targetID)).thenReturn(true);
        when(mockUser.isOwnerOfCompany(companyID)).thenReturn(true);
        when(mockTarget.getParentIDForCompany(companyID)).thenReturn(userID);
        when(mockTarget.getRole(companyID)).thenReturn(mockRole);
        when(mockCompanyHierarchyDomainService.isManagerUnderOwnerTreeTraversal(mockTarget, mockUser, companyID)).thenReturn(true);
        doNothing().when(mockCompanyHierarchyDomainService).removeUserFromCompany(mockTarget, companyID);

        assertTrue(userService.removeOwnerManager(targetID, companyID, "").isSuccess());
    }

    @Test
    void testRemoveManagerFromCompanyTargetNotUnderFail()
    {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;
        User mockUser = mock(User.class);
        User mockTarget=mock(User.class);
        Manager mockRole=mock(Manager.class);
        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);
        when(mockUserRepository.userExists(targetID)).thenReturn(true);
        when(mockUser.isOwnerOfCompany(companyID)).thenReturn(true);
        when(mockTarget.getParentIDForCompany(companyID)).thenReturn(userID);
        when(mockTarget.getRole(companyID)).thenReturn(mockRole);
        when(mockCompanyHierarchyDomainService.isManagerUnderOwnerTreeTraversal(mockTarget, mockUser, companyID)).thenReturn(false);
        doNothing().when(mockCompanyHierarchyDomainService).removeUserFromCompany(mockTarget, companyID);

        assertFalse(userService.removeOwnerManager(targetID, companyID, "").isSuccess());
    }

    @Test
    void testRemoveManagerFromCompanyUserNotOwnerFail()
    {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;
        User mockUser = mock(User.class);
        User mockTarget=mock(User.class);
        Manager mockRole=mock(Manager.class);
        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);
        when(mockUserRepository.userExists(targetID)).thenReturn(true);
        when(mockUser.isOwnerOfCompany(companyID)).thenReturn(false);
        when(mockTarget.getParentIDForCompany(companyID)).thenReturn(userID);
        when(mockTarget.getRole(companyID)).thenReturn(mockRole);
        when(mockCompanyHierarchyDomainService.isManagerUnderOwnerTreeTraversal(mockTarget, mockUser, companyID)).thenReturn(true);
        doNothing().when(mockCompanyHierarchyDomainService).removeUserFromCompany(mockTarget, companyID);

        assertFalse(userService.removeOwnerManager(targetID, companyID, "").isSuccess());
    }
    
    @Test
    void testRemoveManagerFromCompanyTargetNotPersonalFail()
    {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;
        User mockUser = mock(User.class);
        User mockTarget=mock(User.class);
        Manager mockRole=mock(Manager.class);
        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);
        when(mockUserRepository.userExists(targetID)).thenReturn(true);
        when(mockUser.isOwnerOfCompany(companyID)).thenReturn(true);
        when(mockTarget.getParentIDForCompany(companyID)).thenReturn(userID);
        when(mockTarget.getRole(companyID)).thenReturn(null);
        when(mockCompanyHierarchyDomainService.isManagerUnderOwnerTreeTraversal(mockTarget, mockUser, companyID)).thenReturn(true);
        doNothing().when(mockCompanyHierarchyDomainService).removeUserFromCompany(mockTarget, companyID);

        assertFalse(userService.removeOwnerManager(targetID, companyID, "").isSuccess());
    }

    @Test
    void testRemoveManagerFromCompanyAuthFail()
    {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;
        User mockUser = mock(User.class);
        User mockTarget=mock(User.class);
        Manager mockRole=mock(Manager.class);
        when(mockAuthService.validateToken(anyString())).thenReturn(false);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);
        when(mockUserRepository.userExists(targetID)).thenReturn(true);
        when(mockUser.isOwnerOfCompany(companyID)).thenReturn(true);
        when(mockTarget.getParentIDForCompany(companyID)).thenReturn(userID);
        when(mockTarget.getRole(companyID)).thenReturn(mockRole);
        when(mockCompanyHierarchyDomainService.isManagerUnderOwnerTreeTraversal(mockTarget, mockUser, companyID)).thenReturn(true);
        doNothing().when(mockCompanyHierarchyDomainService).removeUserFromCompany(mockTarget, companyID);

        assertFalse(userService.removeOwnerManager(targetID, companyID, "").isSuccess());
    }

}
