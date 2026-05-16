package com.group16b.ApplicationLayer;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.OptimisticLockingFailureException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.DomainServices.CompanyHierarchyDomainService;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.Roles.Manager;
import com.group16b.DomainLayer.User.Roles.ManagerPermissions;
import com.group16b.DomainLayer.User.Roles.Owner;

import io.jsonwebtoken.JwtException;

import com.group16b.DomainLayer.User.User;
public class CompanyHierarchyServiceTests {
    CompanyHierarchyService userService;
    IAuthenticationService mockAuthService;
    IUserRepository mockUserRepository;
    IProductionCompanyRepository mockProductionCompanyRepository;
    CompanyHierarchyDomainService mockCompanyHierarchyDomainService;

    @BeforeEach
    void setUp() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        mockUserRepository = mock(IUserRepository.class);
        mockAuthService = mock(IAuthenticationService.class);
        mockCompanyHierarchyDomainService=mock(CompanyHierarchyDomainService.class);
        mockProductionCompanyRepository=mock(IProductionCompanyRepository.class);

        userService = new CompanyHierarchyService(mockAuthService, mockCompanyHierarchyDomainService,mockProductionCompanyRepository);
        Field userRepo = userService.getClass().getDeclaredField("userRepository");
        userRepo.setAccessible(true);
        userRepo.set(userService, mockUserRepository);
    }

    //good
    @Test
    void GivenValidAuthAndExistingUsersAndCompany_WhenAssignOwnerToCompany_ThenReturnSuccess() {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;

        String token = "valid-token";

        User mockUser = mock(User.class);
        User mockTarget = mock(User.class);
        ProductionCompany mockCompany = mock(ProductionCompany.class);

        // AUTH
        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn(String.valueOf(userID));

        // USERS EXIST
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);

        // COMPANY
        when(mockProductionCompanyRepository.findByID(String.valueOf(companyID))).thenReturn(mockCompany);

        // SAVE
        doNothing().when(mockProductionCompanyRepository).save(mockCompany);

        // ACT
        Result<Boolean> result =userService.assignOwnerToCompany(companyID, targetID, token);

        // ASSERT
        assertTrue(result.isSuccess());

        // VERIFY BEHAVIOR
        verify(mockCompany, times(1)).AssignOwner(userID, targetID);
        verify(mockProductionCompanyRepository, times(1)).save(mockCompany);

        verify(mockUserRepository, times(1)).getUserByID(userID);
        verify(mockUserRepository, times(1)).getUserByID(targetID);
        verify(mockProductionCompanyRepository, times(1))
                .findByID(String.valueOf(companyID));
    }

    //invariant violation
    @Test
    void GivenTargetAlreadyOwner_WhenAssignOwnerToCompany_ThenReturnFailure() {
        String token = "token";

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

        User mockUser = mock(User.class);
        User mockTarget = mock(User.class);
        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockUserRepository.getUserByID(1)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(2)).thenReturn(mockTarget);

        when(mockProductionCompanyRepository.findByID("1"))
                .thenReturn(mockCompany);

        doThrow(new IllegalArgumentException("Target already owner"))
                .when(mockCompany).AssignOwner(1, 2);

        Result<Boolean> result =
                userService.assignOwnerToCompany(1, 2, token);

        assertFalse(result.isSuccess());

        verify(mockProductionCompanyRepository, never()).save(mockCompany);
    }

    //user not found
    @Test
    void GivenCallerUserNotFound_WhenAssignOwnerToCompany_ThenReturnFailure() {
        String token = "token";

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

        when(mockUserRepository.getUserByID(1))
                .thenThrow(new IllegalArgumentException("User not found"));

        Result<Boolean> result =
                userService.assignOwnerToCompany(1, 2, token);

        assertFalse(result.isSuccess());
    }

    //target user not found
    @Test
    void GivenTargetUserNotFound_WhenAssignOwnerToCompany_ThenReturnFailure() {
        String token = "token";

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

        User mockUser = mock(User.class);

        when(mockUserRepository.getUserByID(1)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(2))
                .thenThrow(new IllegalArgumentException("Target not found"));

        Result<Boolean> result =userService.assignOwnerToCompany(1, 2, token);

        assertFalse(result.isSuccess());
    }
    
    //company not found
    @Test
    void GivenCompanyNotFound_WhenAssignOwnerToCompany_ThenReturnFailure() {
        String token = "token";

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

        when(mockUserRepository.getUserByID(1)).thenReturn(mock(User.class));
        when(mockUserRepository.getUserByID(2)).thenReturn(mock(User.class));

        when(mockProductionCompanyRepository.findByID("1"))
                .thenThrow(new IllegalArgumentException("Company not found"));

        Result<Boolean> result =
                userService.assignOwnerToCompany(1, 2, token);

        assertFalse(result.isSuccess());
    }


    //auth fail
    @Test
    void GivenInvalidToken_WhenAssignOwnerToCompany_ThenReturnFailure() {
        when(mockAuthService.validateToken("bad")).thenReturn(false);

        Result<Boolean> result =
                userService.assignOwnerToCompany(1, 2, "bad");

        assertFalse(result.isSuccess());

        verifyNoInteractions(mockUserRepository);
        verifyNoInteractions(mockProductionCompanyRepository);
    }
    //not user token
    @Test
    void GivenNonUserToken_WhenAssignOwnerToCompany_ThenReturnFailure() {
        when(mockAuthService.validateToken("token")).thenReturn(true);
        when(mockAuthService.isUserToken("token")).thenReturn(false);

        Result<Boolean> result = userService.assignOwnerToCompany(1, 2, "token");

        assertFalse(result.isSuccess());

        verifyNoInteractions(mockUserRepository);
        verifyNoInteractions(mockProductionCompanyRepository);
    }

    //save fail
    @Test
    void GivenOptimisticLockFailure_WhenAssignOwnerToCompany_ThenReturnFailure() {
        String token = "token";

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

        User mockUser = mock(User.class);
        User mockTarget = mock(User.class);
        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockUserRepository.getUserByID(1)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(2)).thenReturn(mockTarget);

        when(mockProductionCompanyRepository.findByID("1")).thenReturn(mockCompany);

        doThrow(new OptimisticLockingFailureException("version conflict")).when(mockProductionCompanyRepository).save(mockCompany);

        Result<Boolean> result =userService.assignOwnerToCompany(1, 2, token);

        assertFalse(result.isSuccess());
    }

    
    //---------------------------------------------------------------------
    //    ADD MANAGER ASSIGMENT TESTS
    //----------------------------------------------------------------------

    @Test
    void GivenValidAuthAndExistingUsersAndCompany_WhenAssignManagerToCompany_ThenReturnSuccess() {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;

        String token = "valid-token";

        Set<ManagerPermissions> perms = EnumSet.allOf(ManagerPermissions.class);

        User mockUser = mock(User.class);
        User mockTarget = mock(User.class);
        ProductionCompany mockCompany = mock(ProductionCompany.class);

        // AUTH
        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token))
                .thenReturn(String.valueOf(userID));

        // USERS
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);

        // COMPANY
        when(mockProductionCompanyRepository.findByID(String.valueOf(companyID)))
                .thenReturn(mockCompany);

        doNothing().when(mockCompany).AssignManager(userID, targetID, perms);
        doNothing().when(mockProductionCompanyRepository).save(mockCompany);

        // ACT
        Result<Boolean> result =
                userService.assignManagerToCompany(companyID, targetID, perms, token);

        // ASSERT
        assertTrue(result.isSuccess());

        // VERIFY
        verify(mockCompany, times(1))
                .AssignManager(userID, targetID, perms);

        verify(mockProductionCompanyRepository, times(1)).save(mockCompany);

        verify(mockUserRepository).getUserByID(userID);
        verify(mockUserRepository).getUserByID(targetID);
    }

   @Test
    void GivenCallerUserNotFound_WhenAssignManagerToCompany_ThenReturnFailure() {
        String token = "token";

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

        when(mockUserRepository.getUserByID(1))
                .thenThrow(new IllegalArgumentException("User not found"));

        Result<Boolean> result =
                userService.assignManagerToCompany(1, 2, Collections.emptySet(), token);

        assertFalse(result.isSuccess());
    }

    @Test
    void GivenTargetUserNotFound_WhenAssignManagerToCompany_ThenReturnFailure() {
        String token = "token";

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

        User mockUser = mock(User.class);

        when(mockUserRepository.getUserByID(1)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(2))
                .thenThrow(new IllegalArgumentException("Target not found"));

        Result<Boolean> result =
                userService.assignManagerToCompany(1, 2, Collections.emptySet(), token);

        assertFalse(result.isSuccess());
    }

    @Test
    void GivenCompanyNotFound_WhenAssignManagerToCompany_ThenReturnFailure() {
        String token = "token";

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

        when(mockUserRepository.getUserByID(1)).thenReturn(mock(User.class));
        when(mockUserRepository.getUserByID(2)).thenReturn(mock(User.class));

        when(mockProductionCompanyRepository.findByID("1"))
                .thenThrow(new IllegalArgumentException("Company not found"));

        Result<Boolean> result =
                userService.assignManagerToCompany(1, 2, Collections.emptySet(), token);

        assertFalse(result.isSuccess());
    }

    @Test
    void GivenDomainRuleViolation_WhenAssignManagerToCompany_ThenReturnFailure() {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;

        String token = "valid-token";

        Set<ManagerPermissions> perms = EnumSet.allOf(ManagerPermissions.class);

        User mockUser = mock(User.class);
        User mockTarget = mock(User.class);
        ProductionCompany mockCompany = mock(ProductionCompany.class);

        // AUTH
        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token))
                .thenReturn(String.valueOf(userID));

        // USERS
        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);

        // COMPANY
        when(mockProductionCompanyRepository.findByID(String.valueOf(companyID)))
                .thenReturn(mockCompany);

        // DOMAIN FAILURE
        doThrow(new IllegalArgumentException("Target already manager"))
                .when(mockCompany)
                .AssignManager(userID, targetID, perms);

        // ACT
        Result<Boolean> result =
                userService.assignManagerToCompany(companyID, targetID, perms, token);

        // ASSERT
        assertFalse(result.isSuccess());

        // IMPORTANT: ensure save is NOT called
        verify(mockProductionCompanyRepository, never()).save(mockCompany);
    }


    @Test
    void GivenInvalidToken_WhenAssignManagerToCompany_ThenReturnFailure() {
        when(mockAuthService.validateToken("bad")).thenReturn(false);

        Result<Boolean> result =
                userService.assignManagerToCompany(1, 2, Collections.emptySet(), "bad");

        assertFalse(result.isSuccess());

        verifyNoInteractions(mockUserRepository);
        verifyNoInteractions(mockProductionCompanyRepository);
    }

    @Test
    void GivenNonUserToken_WhenAssignManagerToCompany_ThenReturnFailure() {
        when(mockAuthService.validateToken("token")).thenReturn(true);
        when(mockAuthService.isUserToken("token")).thenReturn(false);

        Result<Boolean> result =
                userService.assignManagerToCompany(1, 2, Collections.emptySet(), "token");

        assertFalse(result.isSuccess());

        verifyNoInteractions(mockUserRepository);
        verifyNoInteractions(mockProductionCompanyRepository);
    }

    @Test
    void GivenOptimisticLockFailure_WhenAssignManagerToCompany_ThenReturnFailure() {
        String token = "token";

        Set<ManagerPermissions> perms = EnumSet.allOf(ManagerPermissions.class);

        User mockUser = mock(User.class);
        User mockTarget = mock(User.class);
        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

        when(mockUserRepository.getUserByID(1)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(2)).thenReturn(mockTarget);

        when(mockProductionCompanyRepository.findByID("1")).thenReturn(mockCompany);

        doThrow(new OptimisticLockingFailureException("fail"))
                .when(mockProductionCompanyRepository).save(mockCompany);

        Result<Boolean> result =
                userService.assignManagerToCompany(1, 2, perms, token);

        assertFalse(result.isSuccess());
    }




    //----------------------------------------------------------------------
    //    ACCEPT INVITE ASSIGMENT TESTS
    //----------------------------------------------------------------------

    @Test
    void GivenValidInvite_WhenAcceptInviteToCompany_ThenReturnSuccess() {
        int userID = 1;
        int companyID = 1;
        int assignerID = 2;
        String token = "token";

        User mockUser = mock(User.class);
        User mockAssigner = mock(User.class);
        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn(String.valueOf(userID));

        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(assignerID)).thenReturn(mockAssigner);

        when(mockProductionCompanyRepository.findByID(String.valueOf(companyID)))
                .thenReturn(mockCompany);

        doNothing().when(mockCompany).acceptInvite(userID, assignerID);
        doNothing().when(mockProductionCompanyRepository).save(mockCompany);

        Result<Boolean> result =
                userService.acceptInviteToCompany(companyID, assignerID, token);

        assertTrue(result.isSuccess());

        verify(mockCompany).acceptInvite(userID, assignerID);
        verify(mockProductionCompanyRepository).save(mockCompany);
    }

    @Test
    void GivenInvalidToken_WhenAcceptInviteToCompany_ThenReturnFail() {
        when(mockAuthService.validateToken("bad")).thenReturn(false);

        Result<Boolean> result =
                userService.acceptInviteToCompany(1, 2, "bad");

        assertFalse(result.isSuccess());

        verifyNoInteractions(mockUserRepository);
        verifyNoInteractions(mockProductionCompanyRepository);
    }

    @Test
    void GivenNonUserToken_WhenAcceptInviteToCompany_ThenReturnFail() {
        when(mockAuthService.validateToken("token")).thenReturn(true);
        when(mockAuthService.isUserToken("token")).thenReturn(false);

        Result<Boolean> result =
                userService.acceptInviteToCompany(1, 2, "token");

        assertFalse(result.isSuccess());
    }

    @Test
    void GivenMissingUser_WhenAcceptInviteToCompany_ThenReturnFail() {
        String token = "token";

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

        when(mockUserRepository.getUserByID(1))
                .thenThrow(new IllegalArgumentException("User not found"));

        Result<Boolean> result =
                userService.acceptInviteToCompany(1, 2, token);

        assertFalse(result.isSuccess());
    }

    @Test
    void GivenMissingAssigner_WhenAcceptInviteToCompany_ThenReturnFail() {
        String token = "token";

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

        User mockUser = mock(User.class);

        when(mockUserRepository.getUserByID(1)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(2))
                .thenThrow(new IllegalArgumentException("Assigner not found"));

        Result<Boolean> result =
                userService.acceptInviteToCompany(1, 2, token);

        assertFalse(result.isSuccess());
    }

    @Test
    void GivenMissingCompany_WhenAcceptInviteToCompany_ThenReturnFail() {
        String token = "token";

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

        when(mockUserRepository.getUserByID(1)).thenReturn(mock(User.class));
        when(mockUserRepository.getUserByID(2)).thenReturn(mock(User.class));

        when(mockProductionCompanyRepository.findByID("1"))
                .thenThrow(new IllegalArgumentException("Company not found"));

        Result<Boolean> result =
                userService.acceptInviteToCompany(1, 2, token);

        assertFalse(result.isSuccess());
    }

    @Test
    void GivenInvalidInvite_WhenAcceptInviteToCompany_ThenReturnFail() {
        String token = "token";

        User mockUser = mock(User.class);
        User mockAssigner = mock(User.class);
        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

        when(mockUserRepository.getUserByID(1)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(2)).thenReturn(mockAssigner);

        when(mockProductionCompanyRepository.findByID("1"))
                .thenReturn(mockCompany);

        doThrow(new IllegalArgumentException("Invite not found"))
                .when(mockCompany).acceptInvite(1, 2);

        Result<Boolean> result =
                userService.acceptInviteToCompany(1, 2, token);

        assertFalse(result.isSuccess());

        verify(mockProductionCompanyRepository, never()).save(mockCompany);
    }

    @Test
    void GivenSaveConflict_WhenAcceptInviteToCompany_ThenReturnFail() {
        String token = "token";

        User mockUser = mock(User.class);
        User mockAssigner = mock(User.class);
        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

        when(mockUserRepository.getUserByID(1)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(2)).thenReturn(mockAssigner);

        when(mockProductionCompanyRepository.findByID("1"))
                .thenReturn(mockCompany);

        doThrow(new OptimisticLockingFailureException("conflict"))
                .when(mockProductionCompanyRepository).save(mockCompany);

        Result<Boolean> result =
                userService.acceptInviteToCompany(1, 2, token);

        assertFalse(result.isSuccess());
    }


    //----------------------------------------------------------------------
    //    REJECT INVITE ASSIGMENT TESTS
    //----------------------------------------------------------------------
    @Test
    void GivenValidInvite_WhenRejectInviteToCompany_ThenReturnSuccess() {
        int userID = 1;
        int companyID = 1;
        int assignerID = 2;
        String token = "token";

        User mockUser = mock(User.class);
        User mockAssigner = mock(User.class);
        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn(String.valueOf(userID));

        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(assignerID)).thenReturn(mockAssigner);

        when(mockProductionCompanyRepository.findByID(String.valueOf(companyID)))
                .thenReturn(mockCompany);

        doNothing().when(mockCompany).rejectInvite(userID, assignerID);
        doNothing().when(mockProductionCompanyRepository).save(mockCompany);

        Result<Boolean> result =
                userService.rejectInviteToCompany(companyID, assignerID, token);

        assertTrue(result.isSuccess());

        verify(mockCompany).rejectInvite(userID, assignerID);
        verify(mockProductionCompanyRepository).save(mockCompany);
    }

    @Test
    void GivenInvalidToken_WhenRejectInviteToCompany_ThenReturnFail() {
        when(mockAuthService.validateToken("bad")).thenReturn(false);

        Result<Boolean> result =
                userService.rejectInviteToCompany(1, 2, "bad");

        assertFalse(result.isSuccess());

        verifyNoInteractions(mockUserRepository);
        verifyNoInteractions(mockProductionCompanyRepository);
    }

    @Test
    void GivenNonUserToken_WhenRejectInviteToCompany_ThenReturnFail() {
        when(mockAuthService.validateToken("token")).thenReturn(true);
        when(mockAuthService.isUserToken("token")).thenReturn(false);

        Result<Boolean> result =
                userService.rejectInviteToCompany(1, 2, "token");

        assertFalse(result.isSuccess());
    }

    @Test
    void GivenMissingUser_WhenRejectInviteToCompany_ThenReturnFail() {
        String token = "token";

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

        when(mockUserRepository.getUserByID(1))
                .thenThrow(new IllegalArgumentException("User not found"));

        Result<Boolean> result =
                userService.rejectInviteToCompany(1, 2, token);

        assertFalse(result.isSuccess());
    }

    @Test
    void GivenMissingAssigner_WhenRejectInviteToCompany_ThenReturnFail() {
        String token = "token";

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

        User mockUser = mock(User.class);

        when(mockUserRepository.getUserByID(1)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(2))
                .thenThrow(new IllegalArgumentException("Assigner not found"));

        Result<Boolean> result =
                userService.rejectInviteToCompany(1, 2, token);

        assertFalse(result.isSuccess());
    }

    @Test
    void GivenMissingCompany_WhenRejectInviteToCompany_ThenReturnFail() {
        String token = "token";

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

        when(mockUserRepository.getUserByID(1)).thenReturn(mock(User.class));
        when(mockUserRepository.getUserByID(2)).thenReturn(mock(User.class));

        when(mockProductionCompanyRepository.findByID("1"))
                .thenThrow(new IllegalArgumentException("Company not found"));

        Result<Boolean> result =
                userService.rejectInviteToCompany(1, 2, token);

        assertFalse(result.isSuccess());
    }

    @Test
    void GivenNoInvite_WhenRejectInviteToCompany_ThenReturnFail() {
        String token = "token";

        User mockUser = mock(User.class);
        User mockAssigner = mock(User.class);
        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

        when(mockUserRepository.getUserByID(1)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(2)).thenReturn(mockAssigner);

        when(mockProductionCompanyRepository.findByID("1"))
                .thenReturn(mockCompany);

        doThrow(new IllegalArgumentException("Invite not found"))
                .when(mockCompany).rejectInvite(1, 2);

        Result<Boolean> result =
                userService.rejectInviteToCompany(1, 2, token);

        assertFalse(result.isSuccess());

        verify(mockProductionCompanyRepository, never()).save(mockCompany);
    }

    @Test
    void GivenSaveConflict_WhenRejectInviteToCompany_ThenReturnFail() {
        String token = "token";

        User mockUser = mock(User.class);
        User mockAssigner = mock(User.class);
        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

        when(mockUserRepository.getUserByID(1)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(2)).thenReturn(mockAssigner);

        when(mockProductionCompanyRepository.findByID("1"))
                .thenReturn(mockCompany);

        doThrow(new OptimisticLockingFailureException("conflict"))
                .when(mockProductionCompanyRepository).save(mockCompany);

        Result<Boolean> result =
                userService.rejectInviteToCompany(1, 2, token);

        assertFalse(result.isSuccess());
    }

    //-----------------------------------------------------------------
    //   FORFEIT OWNERSHIP TESTS
    //-----------------------------------------------------------------
    @Test
    void GivenValidOwner_WhenForfeitOwnership_ThenReturnSuccess() {
        int userID = 1;
        int companyID = 1;
        String token = "token";

        User mockUser = mock(User.class);
        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn(String.valueOf(userID));

        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockProductionCompanyRepository.findByID(String.valueOf(companyID)))
                .thenReturn(mockCompany);

        doNothing().when(mockCompany).forfeitOwnership(userID);
        doNothing().when(mockProductionCompanyRepository).save(mockCompany);

        Result<Boolean> result =
                userService.forfeitOwnership(companyID, token);

        assertTrue(result.isSuccess());

        verify(mockCompany).forfeitOwnership(userID);
        verify(mockProductionCompanyRepository).save(mockCompany);
    }

    @Test
    void GivenUserNotOwner_WhenForfeitOwnership_ThenReturnFailure() {
        int userID = 1;
        int companyID = 1;
        String token = "token";

        User mockUser = mock(User.class);
        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn(String.valueOf(userID));

        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockProductionCompanyRepository.findByID(String.valueOf(companyID)))
                .thenReturn(mockCompany);

        doThrow(new IllegalArgumentException("User not owner"))
                .when(mockCompany).forfeitOwnership(userID);

        Result<Boolean> result =
                userService.forfeitOwnership(companyID, token);

        assertFalse(result.isSuccess());

        verify(mockProductionCompanyRepository, never()).save(any());
    }

    @Test
    void GivenMissingUser_WhenForfeitOwnership_ThenReturnFailure() {
        int userID = 1;
        int companyID = 1;
        String token = "token";

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn(String.valueOf(userID));

        when(mockUserRepository.getUserByID(userID))
                .thenThrow(new IllegalArgumentException("User not found"));

        Result<Boolean> result =
                userService.forfeitOwnership(companyID, token);

        assertFalse(result.isSuccess());

        verifyNoInteractions(mockProductionCompanyRepository);
    }

    @Test
    void GivenMissingCompany_WhenForfeitOwnership_ThenReturnFailure() {
        int userID = 1;
        int companyID = 1;
        String token = "token";

        User mockUser = mock(User.class);

        when(mockAuthService.validateToken(token)).thenReturn(true);
        when(mockAuthService.isUserToken(token)).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(token)).thenReturn(String.valueOf(userID));

        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);

        when(mockProductionCompanyRepository.findByID(String.valueOf(companyID)))
                .thenThrow(new IllegalArgumentException("Company not found"));

        Result<Boolean> result =
                userService.forfeitOwnership(companyID, token);

        assertFalse(result.isSuccess());

        verify(mockProductionCompanyRepository, never()).save(any());
    }

    @Test
    void GivenInvalidToken_WhenForfeitOwnership_ThenReturnFailure() {
        when(mockAuthService.validateToken("bad")).thenReturn(false);

        Result<Boolean> result =
                userService.forfeitOwnership(1, "bad");

        assertFalse(result.isSuccess());

        verifyNoInteractions(mockUserRepository);
        verifyNoInteractions(mockProductionCompanyRepository);
    }

    //------------------------------------------------------------------------------------------
    //     REMOVE PERSONAL FROM COMPANY TESTS
    //------------------------------------------------------------------------------------------



    @Test
    void GivenValidRequest_WhenRemoveOwnerManager_ThenReturnSuccess() {
        int userID = 1;
        int companyID = 1;
        int targetID = 2;

        User mockUser = mock(User.class);
        User mockTarget = mock(User.class);
        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));

        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);

        when(mockProductionCompanyRepository.findByID(String.valueOf(companyID))).thenReturn(mockCompany);

        doNothing().when(mockCompany).removeMemberByOwner(userID, targetID);
        doNothing().when(mockProductionCompanyRepository).save(mockCompany);

        assertTrue(userService.removeOwnerManager(targetID, companyID, "token").isSuccess());
    }

    @Test
    void GivenInvalidToken_WhenRemoveOwnerManager_ThenReturnFail() {
        when(mockAuthService.validateToken(anyString())).thenReturn(false);

        assertFalse(userService.removeOwnerManager(2, 1, "token").isSuccess());
    }

    @Test
    void GivenNonUserToken_WhenRemoveOwnerManager_ThenReturnFail() {
        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.isUserToken(anyString())).thenReturn(false);

        assertFalse(userService.removeOwnerManager(2, 1, "token").isSuccess());
    }

    @Test
    void GivenUserNotFound_WhenRemoveOwnerManager_ThenReturnFail() {
        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn("1");

        when(mockUserRepository.getUserByID(anyInt())).thenThrow(new IllegalArgumentException("User not found"));

        assertFalse(userService.removeOwnerManager(2, 1, "token").isSuccess());
    }

    @Test
    void GivenTargetUserNotFound_WhenRemoveOwnerManager_ThenReturnFail() {
        int userID = 1;

        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));

        when(mockUserRepository.getUserByID(userID)).thenReturn(mock(User.class));
        when(mockUserRepository.getUserByID(2)).thenThrow(new IllegalArgumentException("Target not found"));

        assertFalse(userService.removeOwnerManager(2, 1, "token").isSuccess());
    }

    @Test
    void GivenCompanyNotFound_WhenRemoveOwnerManager_ThenReturnFail() {
        int userID = 1;

        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));

        when(mockUserRepository.getUserByID(anyInt())).thenReturn(mock(User.class));
        when(mockProductionCompanyRepository.findByID(anyString()))
                .thenThrow(new IllegalArgumentException("Company not found"));

        assertFalse(userService.removeOwnerManager(2, 1, "token").isSuccess());
    }

    @Test
    void GivenRemoveMemberThrowsException_WhenRemoveOwnerManager_ThenReturnFail() {
        int userID = 1;

        User mockUser = mock(User.class);
        User mockTarget = mock(User.class);
        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));

        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(2)).thenReturn(mockTarget);

        when(mockProductionCompanyRepository.findByID(anyString())).thenReturn(mockCompany);

        doThrow(new IllegalArgumentException("Not allowed"))
                .when(mockCompany).removeMemberByOwner(userID, 2);

        assertFalse(userService.removeOwnerManager(2, 1, "token").isSuccess());
    }

    @Test
    void GivenSaveThrowsOptimisticLocking_WhenRemoveOwnerManager_ThenReturnFail() {
        int userID = 1;

        User mockUser = mock(User.class);
        User mockTarget = mock(User.class);
        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));

        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(2)).thenReturn(mockTarget);

        when(mockProductionCompanyRepository.findByID(anyString())).thenReturn(mockCompany);

        doNothing().when(mockCompany).removeMemberByOwner(userID, 2);
        doThrow(new OptimisticLockingFailureException("conflict"))
                .when(mockProductionCompanyRepository).save(mockCompany);

        assertFalse(userService.removeOwnerManager(2, 1, "token").isSuccess());
    }

    @Test
    void GivenJwtException_WhenRemoveOwnerManager_ThenReturnFail() {
        when(mockAuthService.validateToken(anyString()))
                .thenThrow(new JwtException("bad token"));

        assertFalse(userService.removeOwnerManager(2, 1, "token").isSuccess());
    }

    @Test
    void GivenUnexpectedException_WhenRemoveOwnerManager_ThenReturnFail() {
        when(mockAuthService.validateToken(anyString()))
                .thenThrow(new RuntimeException("unexpected"));

        assertFalse(userService.removeOwnerManager(2, 1, "token").isSuccess());
    }

    @Test
    void GivenValidRequest_WhenChangeManagerPermission_ThenReturnSuccess() {
        int userID = 1;
        int targetID = 2;
        int companyID = 10;

        Set<ManagerPermissions> permissions =Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

        User mockUser = mock(User.class);
        User mockTarget = mock(User.class);
        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString()))
                .thenReturn(String.valueOf(userID));

        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(targetID)).thenReturn(mockTarget);

        when(mockProductionCompanyRepository.findByID(String.valueOf(companyID))).thenReturn(mockCompany);

        doNothing().when(mockCompany)
                .updatePermissionsOfManager(userID, targetID, permissions);

        doNothing().when(mockProductionCompanyRepository).save(mockCompany);

        assertTrue(userService.changeManagerPermission(
                targetID,
                companyID,
                permissions,
                "token"
        ).isSuccess());
        }

        @Test
        void GivenInvalidToken_WhenChangeManagerPermission_ThenReturnFail() {
        Set<ManagerPermissions> permissions =
                Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

        when(mockAuthService.validateToken(anyString())).thenReturn(false);

        assertFalse(userService.changeManagerPermission(
                2,
                1,
                permissions,
                "token"
        ).isSuccess());
        }

        @Test
        void GivenNonUserToken_WhenChangeManagerPermission_ThenReturnFail() {
        Set<ManagerPermissions> permissions =
                Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.isUserToken(anyString())).thenReturn(false);

        assertFalse(userService.changeManagerPermission(
                2,
                1,
                permissions,
                "token"
        ).isSuccess());
        }

        @Test
        void GivenUserNotFound_WhenChangeManagerPermission_ThenReturnFail() {
        Set<ManagerPermissions> permissions =
                Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString()))
                .thenReturn("1");

        when(mockUserRepository.getUserByID(anyInt()))
                .thenThrow(new IllegalArgumentException("User not found"));

        assertFalse(userService.changeManagerPermission(
                2,
                1,
                permissions,
                "token"
        ).isSuccess());
        }

        @Test
        void GivenTargetUserNotFound_WhenChangeManagerPermission_ThenReturnFail() {
        int userID = 1;

        Set<ManagerPermissions> permissions =
                Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString()))
                .thenReturn(String.valueOf(userID));

        when(mockUserRepository.getUserByID(userID))
                .thenReturn(mock(User.class));

        when(mockUserRepository.getUserByID(2))
                .thenThrow(new IllegalArgumentException("Target not found"));

        assertFalse(userService.changeManagerPermission(
                2,
                1,
                permissions,
                "token"
        ).isSuccess());
        }

        @Test
        void GivenCompanyNotFound_WhenChangeManagerPermission_ThenReturnFail() {
        int userID = 1;

        Set<ManagerPermissions> permissions =
                Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString()))
                .thenReturn(String.valueOf(userID));

        when(mockUserRepository.getUserByID(anyInt()))
                .thenReturn(mock(User.class));

        when(mockProductionCompanyRepository.findByID(anyString()))
                .thenThrow(new IllegalArgumentException("Company not found"));

        assertFalse(userService.changeManagerPermission(
                2,
                1,
                permissions,
                "token"
        ).isSuccess());
        }

        @Test
        void GivenUpdatePermissionsThrowsException_WhenChangeManagerPermission_ThenReturnFail() {
        int userID = 1;

        Set<ManagerPermissions> permissions =
                Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

        User mockUser = mock(User.class);
        User mockTarget = mock(User.class);
        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString()))
                .thenReturn(String.valueOf(userID));

        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(2)).thenReturn(mockTarget);

        when(mockProductionCompanyRepository.findByID(anyString()))
                .thenReturn(mockCompany);

        doThrow(new IllegalArgumentException("Not allowed"))
                .when(mockCompany)
                .updatePermissionsOfManager(userID, 2, permissions);

        assertFalse(userService.changeManagerPermission(
                2,
                1,
                permissions,
                "token"
        ).isSuccess());
        }

        @Test
        void GivenSaveThrowsOptimisticLocking_WhenChangeManagerPermission_ThenReturnFail() {
        int userID = 1;

        Set<ManagerPermissions> permissions =
                Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

        User mockUser = mock(User.class);
        User mockTarget = mock(User.class);
        ProductionCompany mockCompany = mock(ProductionCompany.class);

        when(mockAuthService.validateToken(anyString())).thenReturn(true);
        when(mockAuthService.isUserToken(anyString())).thenReturn(true);
        when(mockAuthService.extractSubjectFromToken(anyString()))
                .thenReturn(String.valueOf(userID));

        when(mockUserRepository.getUserByID(userID)).thenReturn(mockUser);
        when(mockUserRepository.getUserByID(2)).thenReturn(mockTarget);

        when(mockProductionCompanyRepository.findByID(anyString()))
                .thenReturn(mockCompany);

        doNothing().when(mockCompany)
                .updatePermissionsOfManager(userID, 2, permissions);

        doThrow(new OptimisticLockingFailureException("conflict"))
                .when(mockProductionCompanyRepository)
                .save(mockCompany);

        assertFalse(userService.changeManagerPermission(
                2,
                1,
                permissions,
                "token"
        ).isSuccess());
        }

        @Test
        void GivenJwtException_WhenChangeManagerPermission_ThenReturnFail() {
        Set<ManagerPermissions> permissions =
                Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

        when(mockAuthService.validateToken(anyString()))
                .thenThrow(new JwtException("bad token"));

        assertFalse(userService.changeManagerPermission(
                2,
                1,
                permissions,
                "token"
        ).isSuccess());
        }

        @Test
        void GivenUnexpectedException_WhenChangeManagerPermission_ThenReturnFail() {
        Set<ManagerPermissions> permissions =
                Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

        when(mockAuthService.validateToken(anyString()))
                .thenThrow(new RuntimeException("unexpected"));

        assertFalse(userService.changeManagerPermission(
                2,
                1,
                permissions,
                "token"
        ).isSuccess());
        }

}