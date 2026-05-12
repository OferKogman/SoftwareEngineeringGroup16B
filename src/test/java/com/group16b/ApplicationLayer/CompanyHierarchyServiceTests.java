package com.group16b.ApplicationLayer;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

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
import com.group16b.DomainLayer.DomainServices.CompanyHierarchyDomainService;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.Roles.Manager;
import com.group16b.DomainLayer.User.Roles.ManagerPermissions;
import com.group16b.DomainLayer.User.Roles.Owner;
import com.group16b.DomainLayer.User.User;
public class CompanyHierarchyServiceTests {
    CompanyHierarchyService userService;
    IAuthenticationService mockAuthService;
    IUserRepository mockUserRepository;
    CompanyHierarchyDomainService mockCompanyHierarchyDomainService;

    @BeforeEach
    void setUp() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        mockUserRepository = mock(IUserRepository.class);
        mockAuthService = mock(IAuthenticationService.class);
        mockCompanyHierarchyDomainService=mock(CompanyHierarchyDomainService.class);
        userService = new CompanyHierarchyService(mockAuthService, mockCompanyHierarchyDomainService);
        Field userRepo = userService.getClass().getDeclaredField("userRepository");
        userRepo.setAccessible(true);
        userRepo.set(userService, mockUserRepository);
    }

    //good
    @Test
    void testAssignOwnerToCompanySuccess() {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;
        User mockUser = mock(User.class);
        User mockTarget = mock(User.class);
        
        // Mock authentication
        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        
        // Mock user repository
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);
        when(mockUserRepository.userExists(targetID)).thenReturn(true);
        
        // Mock user permissions: Ensure the assigning user is an owner
        when(mockUser.isOwnerOfCompany(companyID)).thenReturn(true);
        
        // Mock target user: Ensure the target is NOT already an owner
        when(mockTarget.isOwnerOfCompany(companyID)).thenReturn(false);
        
        // Mock addInvite to do nothing (success case)
        doNothing().when(mockTarget).addInvite(eq(companyID), eq(userID), any(Owner.class));
        
        // Assert success
        assertTrue(userService.assignOwnerToCompany(companyID, targetID, "").isSuccess());
    }

    //user not owner
    @Test
    void testAssignOwnerToCompanyByNonOwnerFail() {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;
        User mockUser = mock(User.class);
        User mockTarget = mock(User.class);
        
        // Mock authentication
        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        
        // Mock user repository
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);
        when(mockUserRepository.userExists(targetID)).thenReturn(true);
        
        // Mock user permissions: Ensure the assigning user is an owner
        when(mockUser.isOwnerOfCompany(companyID)).thenReturn(false);
        
        // Mock target user: Ensure the target is NOT already an owner
        when(mockTarget.isOwnerOfCompany(companyID)).thenReturn(false);
        
        // Mock addInvite to do nothing (success case)
        doNothing().when(mockTarget).addInvite(eq(companyID), eq(userID), any(Owner.class));
        
        // Assert success
        assertFalse(userService.assignOwnerToCompany(companyID, targetID, "").isSuccess());
    }

    //target user not found
    @Test
    void testAssignOwnerToCompanyNonExistingTargetFail() {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;
        User mockUser = mock(User.class);
        User mockTarget = mock(User.class);
        
        // Mock authentication
        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        
        // Mock user repository
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(null);
        when(mockUserRepository.userExists(targetID)).thenReturn(false);
        
        // Mock user permissions: Ensure the assigning user is an owner
        when(mockUser.isOwnerOfCompany(companyID)).thenReturn(true);
        
        // Mock target user: Ensure the target is NOT already an owner
        when(mockTarget.isOwnerOfCompany(companyID)).thenReturn(false);
        
        // Mock addInvite to do nothing (success case)
        doNothing().when(mockTarget).addInvite(eq(companyID), eq(userID), any(Owner.class));
        
        // Assert success
        assertFalse(userService.assignOwnerToCompany(companyID, targetID, "").isSuccess());
    }

    //target user already owner
    @Test
    void testAssignOwnerToCompanyTargetAlreadyOwnerFail() {
int userID = 1;
        int companyID = 1;
        int targetID = 2;
        User mockUser = mock(User.class);
        User mockTarget = mock(User.class);
        
        // Mock authentication
        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        
        // Mock user repository
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);
        when(mockUserRepository.userExists(targetID)).thenReturn(true);
        
        // Mock user permissions: Ensure the assigning user is an owner
        when(mockUser.isOwnerOfCompany(companyID)).thenReturn(true);
        
        // Mock target user: Ensure the target is NOT already an owner
        when(mockTarget.isOwnerOfCompany(companyID)).thenReturn(true);
        
        // Mock addInvite to do nothing (success case)
        doNothing().when(mockTarget).addInvite(eq(companyID), eq(userID), any(Owner.class));
        
        // Assert success
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
        User mockTarget = mock(User.class);
        Manager mockManager=mock(Manager.class);
        Set<ManagerPermissions> perms=EnumSet.allOf(ManagerPermissions.class);
        // Mock authentication
        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        
        // Mock user repository
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);
        when(mockUserRepository.userExists(targetID)).thenReturn(true);
        
        // Mock user permissions: Ensure the assigning user is an owner
        when(mockUser.isOwnerOfCompany(companyID)).thenReturn(true);
        
        // Mock target user: Ensure the target is NOT already an owner
        when(mockTarget.getRole(companyID)).thenReturn(null);

        
        // Mock addInvite to do nothing (success case)
        doNothing().when(mockTarget).addInvite(eq(companyID), eq(userID), any(Manager.class));
        
        // Assert success
        assertTrue(userService.assignManagerToCompany(companyID, targetID,perms, "").isSuccess());
    }

    //user not owner
    @Test
    void testAssignManagerToCompanyByNonOwnerFail() {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;
        User mockUser = mock(User.class);
        User mockTarget = mock(User.class);
        Manager mockManager=mock(Manager.class);
        Set<ManagerPermissions> perms=EnumSet.allOf(ManagerPermissions.class);
        // Mock authentication
        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        
        // Mock user repository
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);
        when(mockUserRepository.userExists(targetID)).thenReturn(true);
        
        // Mock user permissions: Ensure the assigning user is an owner
        when(mockUser.isOwnerOfCompany(companyID)).thenReturn(false);
        
        // Mock target user: Ensure the target is NOT already an owner
        when(mockTarget.getRole(companyID)).thenReturn(null);

        
        // Mock addInvite to do nothing (success case)
        doNothing().when(mockTarget).addInvite(eq(companyID), eq(userID), any(Manager.class));
        
        // Assert success
        assertFalse(userService.assignManagerToCompany(companyID, targetID,perms, "").isSuccess());
    }

    //target user not found
    @Test
    void testAssignManagerToCompanyNonExistingTargetFail() {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;
        User mockUser = mock(User.class);
        User mockTarget = mock(User.class);
        Manager mockManager=mock(Manager.class);
        Set<ManagerPermissions> perms=EnumSet.allOf(ManagerPermissions.class);
        // Mock authentication
        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        
        // Mock user repository
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(null);
        when(mockUserRepository.userExists(targetID)).thenReturn(true);
        
        // Mock user permissions: Ensure the assigning user is an owner
        when(mockUser.isOwnerOfCompany(companyID)).thenReturn(true);
        
        // Mock target user: Ensure the target is NOT already an owner
        when(mockTarget.getRole(companyID)).thenReturn(null);

        
        // Mock addInvite to do nothing (success case)
        doNothing().when(mockTarget).addInvite(eq(companyID), eq(userID), any(Manager.class));
        
        // Assert success
        assertFalse(userService.assignManagerToCompany(companyID, targetID,perms, "").isSuccess());
    }

    //target user already owner
    @Test
    void testAssignManagerToCompanyTargetAlreadyManagerFail() {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;
        User mockUser = mock(User.class);
        User mockTarget = mock(User.class);
        Manager mockManager=mock(Manager.class);
        Set<ManagerPermissions> perms=EnumSet.allOf(ManagerPermissions.class);
        // Mock authentication
        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        
        // Mock user repository
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);
        when(mockUserRepository.userExists(targetID)).thenReturn(true);
        
        // Mock user permissions: Ensure the assigning user is an owner
        when(mockUser.isOwnerOfCompany(companyID)).thenReturn(true);
        
        // Mock target user: Ensure the target is NOT already an owner
        when(mockTarget.getRole(companyID)).thenReturn(mockManager);

        
        // Mock addInvite to do nothing (success case)
        doNothing().when(mockTarget).addInvite(eq(companyID), eq(userID), any(Manager.class));
        
        // Assert success
        assertFalse(userService.assignManagerToCompany(companyID, targetID,perms, "").isSuccess());
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
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.userExists(assignerID)).thenReturn(true);
        when(mockUserRepository.getUserByID(assignerID)).thenReturn(mockAssigner);
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
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.userExists(assignerID)).thenReturn(true);
        when(mockUserRepository.getUserByID(assignerID)).thenReturn(mock(User.class));
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
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
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
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.userExists(assignerID)).thenReturn(true);
        doNothing().when(mockUser).rejectInvite(companyID, assignerID);
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
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.userExists(assignerID)).thenReturn(true);
        doThrow(new IllegalArgumentException("No invite found")).when(mockUser).rejectInvite(companyID, assignerID);

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
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.userExists(assignerID)).thenReturn(true);
        doNothing().when(mockUser).rejectInvite(companyID, assignerID);
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
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
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
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
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
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
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
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
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
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
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
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
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
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
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
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
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
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
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
