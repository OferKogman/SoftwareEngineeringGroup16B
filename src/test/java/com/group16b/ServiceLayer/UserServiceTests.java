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
import com.group16b.ApplicationLayer.UserService;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.Roles.Manager;
import com.group16b.DomainLayer.User.Roles.Owner;
import com.group16b.DomainLayer.User.User;
public class UserServiceTests {
    UserService userService;
    IAuthenticationService mockAuthService;
    IUserRepository mockUserRepository;
    @BeforeEach
    void setUp() {
        mockAuthService = mock(IAuthenticationService.class);
        mockUserRepository = mock(IUserRepository.class);
        userService = new UserService(mockAuthService, mockUserRepository);
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

        when(mockAuthService.authenticate(anyString())).thenReturn(true);
        when(mockAuthService.extractIdFromUserToken(anyString())).thenReturn(userID);
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);
        when(mockUserRepository.userExists(targetID)).thenReturn(true);
        when(mockTarget.getUserInvitesLock()).thenReturn(new ReentrantLock());

        assertTrue(userService.assignOwnerToCompany(userID, companyID, targetID, "").isSuccess());
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

        when(mockAuthService.authenticate(anyString())).thenReturn(true);
        when(mockAuthService.extractIdFromUserToken(anyString())).thenReturn(userID);
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);
        when(mockUserRepository.userExists(targetID)).thenReturn(true);
        when(mockTarget.getUserInvitesLock()).thenReturn(new ReentrantLock());

        assertFalse(userService.assignOwnerToCompany(userID, companyID, targetID, "").isSuccess());
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

        when(mockAuthService.authenticate(anyString())).thenReturn(true);
        when(mockAuthService.extractIdFromUserToken(anyString())).thenReturn(userID);
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(null);
        when(mockUserRepository.userExists(targetID)).thenReturn(false);
        when(mockTarget.getUserInvitesLock()).thenReturn(new ReentrantLock());

        assertFalse(userService.assignOwnerToCompany(userID, companyID, targetID, "").isSuccess());
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
        when(mockAuthService.authenticate(anyString())).thenReturn(true);
        when(mockAuthService.extractIdFromUserToken(anyString())).thenReturn(userID);
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);
        when(mockUserRepository.userExists(targetID)).thenReturn(true);
        when(mockTarget.getUserInvitesLock()).thenReturn(new ReentrantLock());

        assertFalse(userService.assignOwnerToCompany(userID, companyID, targetID, "").isSuccess());
    }


    @Test
    void testAssignOwnerToCompanyInvalidSessionFail() {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;

        when(mockAuthService.authenticate(anyString())).thenReturn(false);

        assertFalse(userService.assignOwnerToCompany(userID, companyID, targetID, "").isSuccess());
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

        when(mockAuthService.authenticate(anyString())).thenReturn(true);
        when(mockAuthService.extractIdFromUserToken(anyString())).thenReturn(userID);
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);
        when(mockUserRepository.userExists(targetID)).thenReturn(true);
        when(mockTarget.getUserInvitesLock()).thenReturn(new ReentrantLock());

        assertTrue(userService.assignManagerToCompany(userID, companyID, targetID, Collections.emptySet(), "").isSuccess());
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

        when(mockAuthService.authenticate(anyString())).thenReturn(true);
        when(mockAuthService.extractIdFromUserToken(anyString())).thenReturn(userID);
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);
        when(mockUserRepository.userExists(targetID)).thenReturn(true);
        when(mockTarget.getUserInvitesLock()).thenReturn(new ReentrantLock());

        assertFalse(userService.assignManagerToCompany(userID, companyID, targetID, Collections.emptySet(), "").isSuccess());
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

        when(mockAuthService.authenticate(anyString())).thenReturn(true);
        when(mockAuthService.extractIdFromUserToken(anyString())).thenReturn(userID);
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(null);
        when(mockUserRepository.userExists(targetID)).thenReturn(false);
        when(mockTarget.getUserInvitesLock()).thenReturn(new ReentrantLock());

        assertFalse(userService.assignManagerToCompany(userID, companyID, targetID, Collections.emptySet(), "").isSuccess());
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
        when(mockAuthService.authenticate(anyString())).thenReturn(true);
        when(mockAuthService.extractIdFromUserToken(anyString())).thenReturn(userID);
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);
        when(mockUserRepository.userExists(targetID)).thenReturn(true);
        when(mockTarget.getUserInvitesLock()).thenReturn(new ReentrantLock());

        assertFalse(userService.assignManagerToCompany(userID, companyID, targetID, Collections.emptySet(), "").isSuccess());
    }


    @Test
    void testAssignManagerToCompanyInvalidSessionFail() {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;

        when(mockAuthService.authenticate(anyString())).thenReturn(false);

        assertFalse(userService.assignManagerToCompany(userID, companyID, targetID, Collections.emptySet(), "").isSuccess());
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
        doNothing().when(mockUser).validatePermissions(anyInt(), eq(Owner.class));
        when(mockAuthService.authenticate(anyString())).thenReturn(true);
        when(mockAuthService.extractIdFromUserToken(anyString())).thenReturn(userID);
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.userExists(assignerID)).thenReturn(true);
        when(mockUserRepository.getUserByID(assignerID)).thenReturn(mock(User.class));
        when(mockUser.getUserInvitesLock()).thenReturn(new ReentrantLock());

        assertTrue(userService.acceptInviteToCompany(userID, companyID, assignerID, "").isSuccess());
    }

    @Test
    void testAcceptInviteAssignmentInvalidSessionFail() {
        int userID = 1;
        int companyID = 1;
        int assignerID = 2;

        when(mockAuthService.authenticate(anyString())).thenReturn(false);

        assertFalse(userService.acceptInviteToCompany(userID, companyID, assignerID, "").isSuccess());
    }

    @Test
    void testAcceptInviteAssignmentNoInviteFail() {
        int userID = 1;
        int companyID = 1;
        int assignerID = 2;
        User mockUser = mock(User.class);
        doNothing().when(mockUser).validatePermissions(anyInt(), eq(Owner.class));
        when(mockAuthService.authenticate(anyString())).thenReturn(true);
        when(mockAuthService.extractIdFromUserToken(anyString())).thenReturn(userID);
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.userExists(assignerID)).thenReturn(true);
        when(mockUserRepository.getUserByID(assignerID)).thenReturn(mock(User.class));
        when(mockUser.getUserInvitesLock()).thenReturn(new ReentrantLock());
        doThrow(new IllegalArgumentException("No invite found")).when(mockUser).acceptInvite(companyID, assignerID);

        assertFalse(userService.acceptInviteToCompany(userID, companyID, assignerID, "").isSuccess());
    }

    @Test
    void testAcceptInviteAssignmentAssignerNotFoundFail() {
        int userID = 1;
        int companyID = 1;
        int assignerID = 2;
        User mockUser = mock(User.class);
        doNothing().when(mockUser).validatePermissions(anyInt(), eq(Owner.class));
        when(mockAuthService.authenticate(anyString())).thenReturn(true);
        when(mockAuthService.extractIdFromUserToken(anyString())).thenReturn(userID);
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.userExists(assignerID)).thenReturn(false);

        assertFalse(userService.acceptInviteToCompany(userID, companyID, assignerID, "").isSuccess());
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
        when(mockAuthService.authenticate(anyString())).thenReturn(true);
        when(mockAuthService.extractIdFromUserToken(anyString())).thenReturn(userID);
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.userExists(assignerID)).thenReturn(true);
        doNothing().when(mockUser).rejectInvite(companyID, assignerID);
        when(mockUser.getUserInvitesLock()).thenReturn(new ReentrantLock());
        assertTrue(userService.rejectInviteToCompany(userID, companyID, assignerID, "").isSuccess());
    }

    @Test
    void testRejectAssignmentNoInviteFailure() {
        int userID = 1;
        int companyID = 1;
        int assignerID = 2;
        User mockUser = mock(User.class);
        doNothing().when(mockUser).validatePermissions(anyInt(), eq(Owner.class));
        when(mockAuthService.authenticate(anyString())).thenReturn(true);
        when(mockAuthService.extractIdFromUserToken(anyString())).thenReturn(userID);
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.userExists(assignerID)).thenReturn(true);
        doThrow(new IllegalArgumentException("No invite found")).when(mockUser).rejectInvite(companyID, assignerID);
        when(mockUser.getUserInvitesLock()).thenReturn(new ReentrantLock());

        assertFalse(userService.rejectInviteToCompany(userID, companyID, assignerID, "").isSuccess());
    }

    @Test
    void testRejectAssignmentBadSessionTokenFailure() {
        int userID = 1;
        int companyID = 1;
        int assignerID = 2;
        User mockUser = mock(User.class);
        doNothing().when(mockUser).validatePermissions(anyInt(), eq(Owner.class));
        when(mockAuthService.authenticate(anyString())).thenReturn(false);
        when(mockAuthService.extractIdFromUserToken(anyString())).thenReturn(userID);
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.userExists(assignerID)).thenReturn(true);
        doNothing().when(mockUser).rejectInvite(companyID, assignerID);
        when(mockUser.getUserInvitesLock()).thenReturn(new ReentrantLock());
        assertFalse(userService.rejectInviteToCompany(userID, companyID, assignerID, "").isSuccess());
    }

}
