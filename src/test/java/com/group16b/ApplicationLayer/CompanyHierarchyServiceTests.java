package com.group16b.ApplicationLayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;

import com.group16b.ApplicationLayer.DTOs.HierarchyNodeDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.membership.HierarchyNodeData;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.ProductionCompany.membership.RoleType;
import com.group16b.DomainLayer.User.User;
import com.group16b.InfrastructureLayer.MapDBs.ProductionCompanyRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;

import io.jsonwebtoken.JwtException;

public class CompanyHierarchyServiceTests {
        private CompanyHierarchyService CompanyHierarchyService;
        private IAuthenticationService mockAuthService;
        private IRepository<User> UserRepository;
        private IProductionCompanyRepository ProductionCompanyRepository;

        private User founder;
        private User owner1;
        private User owner2;
        private User manager1;
        private User manager2;
        private User bystander;
        private User invitedManager;
        private User invitedOwner;

        private final String FOUNDER_EMAIL="founder@example.com";
        private final String OWNER1_EMAIL="owner1@example.com";
        private final String OWNER2_EMAIL="owner2@example.com";
        private final String MANAGER1_EMAIL="manager1@example.com";
        private final String MANAGER2_EMAIL="manager2@example.com";
        private final String INVITED_MANAGER_EMAIL="invited-manager@example.com";
        private final String INVITED_OWNER_EMAIL="invited-owner@example.com";
        private final String BYSTANDER_EMAIL="bystander@example.com";
        private final String BAD_USER_EMAIL="bad@example.com";

        private final String VALID_FOUNDER_TOKEN="valid-founder-token";
        private final String VALID_OWNER1_TOKEN="valid-owner1-token";
        private final String VALID_OWNER2_TOKEN="valid-owner2-token";
        private final String VALID_MANAGER1_TOKEN="valid-manager1-token";
        private final String VALID_MANAGER2_TOKEN="valid-manager2-token";
        private final String VALID_INVITED_MANAGER_TOKEN="valid-invited-manager-token";
        private final String VALID_INVITED_OWNER_TOKEN="valid-invited-owner-token";
        private final String VALID_BYSTANDER_TOKEN="valid-bystander-token";
        private final String INVALID_TOKEN="invalid-token";

        private ProductionCompany company1;
        private ProductionCompany company2;

        private final int COMPANY1_ID=1;
        private final int COMPANY2_ID=2;
        private final int BAD_COMPANY_ID=999;

        @BeforeEach
        void setUp() {
                mockAuthService = mock(IAuthenticationService.class);
                UserRepository = new UserRepositoryMapImpl();
                ProductionCompanyRepository = new ProductionCompanyRepositoryMapImpl();
                CompanyHierarchyService = new CompanyHierarchyService(mockAuthService, ProductionCompanyRepository, UserRepository);

                when(mockAuthService.validateToken(VALID_BYSTANDER_TOKEN)).thenReturn(true);
                when(mockAuthService.isUserToken(VALID_BYSTANDER_TOKEN)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(VALID_BYSTANDER_TOKEN)).thenReturn(BYSTANDER_EMAIL);

                when(mockAuthService.validateToken(VALID_FOUNDER_TOKEN)).thenReturn(true);
                when(mockAuthService.isUserToken(VALID_FOUNDER_TOKEN)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(VALID_FOUNDER_TOKEN)).thenReturn(FOUNDER_EMAIL);

                when(mockAuthService.validateToken(VALID_OWNER1_TOKEN)).thenReturn(true);
                when(mockAuthService.isUserToken(VALID_OWNER1_TOKEN)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(VALID_OWNER1_TOKEN)).thenReturn(OWNER1_EMAIL);

                when(mockAuthService.validateToken(VALID_OWNER2_TOKEN)).thenReturn(true);
                when(mockAuthService.isUserToken(VALID_OWNER2_TOKEN)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(VALID_OWNER2_TOKEN)).thenReturn(OWNER2_EMAIL);

                when(mockAuthService.validateToken(VALID_MANAGER1_TOKEN)).thenReturn(true);
                when(mockAuthService.isUserToken(VALID_MANAGER1_TOKEN)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(VALID_MANAGER1_TOKEN)).thenReturn(MANAGER1_EMAIL);

                when(mockAuthService.validateToken(VALID_MANAGER2_TOKEN)).thenReturn(true);
                when(mockAuthService.isUserToken(VALID_MANAGER2_TOKEN)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(VALID_MANAGER2_TOKEN)).thenReturn(MANAGER2_EMAIL);

                when(mockAuthService.validateToken(VALID_INVITED_MANAGER_TOKEN)).thenReturn(true);
                when(mockAuthService.isUserToken(VALID_INVITED_MANAGER_TOKEN)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(VALID_INVITED_MANAGER_TOKEN)).thenReturn(INVITED_MANAGER_EMAIL);

                when(mockAuthService.validateToken(VALID_INVITED_OWNER_TOKEN)).thenReturn(true);
                when(mockAuthService.isUserToken(VALID_INVITED_OWNER_TOKEN)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(VALID_INVITED_OWNER_TOKEN)).thenReturn(INVITED_OWNER_EMAIL);                
                
                when(mockAuthService.validateToken(INVALID_TOKEN)).thenReturn(false);

                seedBaseData();
        }

        private void seedBaseData() {
                founder = new User(FOUNDER_EMAIL, "password");
                owner1 = new User(OWNER1_EMAIL, "password");
                owner2 = new User(OWNER2_EMAIL, "password");
                manager1 = new User(MANAGER1_EMAIL, "password");
                manager2 = new User(MANAGER2_EMAIL, "password");
                invitedManager = new User(INVITED_MANAGER_EMAIL, "password");
                invitedOwner = new User(INVITED_OWNER_EMAIL, "password");
                bystander = new User(BYSTANDER_EMAIL, "password");

                UserRepository.save(founder);
                UserRepository.save(owner1);
                UserRepository.save(owner2);
                UserRepository.save(manager1);
                UserRepository.save(manager2);
                UserRepository.save(invitedManager);
                UserRepository.save(invitedOwner);
                UserRepository.save(bystander);

                company1 = new ProductionCompany(COMPANY1_ID, "Company One", 0.1, FOUNDER_EMAIL);
                company2 = new ProductionCompany(COMPANY2_ID, "Company Two", 0.2, FOUNDER_EMAIL);
                
                assignOwner(company1, FOUNDER_EMAIL, OWNER1_EMAIL);
                assignOwner(company1, OWNER1_EMAIL, OWNER2_EMAIL);
                assignMaager(company1, OWNER1_EMAIL, MANAGER1_EMAIL, EnumSet.of(ManagerPermissions.CUSTOMER_SUPPORT));
                assignMaager(company1, OWNER2_EMAIL, MANAGER2_EMAIL, EnumSet.of(ManagerPermissions.PURCHASE_POLICY));

                company1.AssignManager(INVITED_MANAGER_EMAIL, OWNER1_EMAIL, EnumSet.allOf(ManagerPermissions.class));
                company1.AssignOwner(INVITED_OWNER_EMAIL, OWNER2_EMAIL);

                ProductionCompanyRepository.save(company1);
                ProductionCompanyRepository.save(company2);
        }

        private void assignMaager(ProductionCompany company, String assignerEmail, String managerEmail, Set<ManagerPermissions> perms)
        {
                company.AssignManager(assignerEmail, managerEmail, perms);
                company.acceptInvite(managerEmail, assignerEmail);
        }
        private void assignOwner(ProductionCompany company, String assignerEmail, String newOwnerEmail)
        {
                company.AssignOwner(assignerEmail, newOwnerEmail);
                company.acceptInvite(newOwnerEmail, assignerEmail);
        }

        // good
        @Test
        void GivenValidAuthAndExistingUsersAndCompany_WhenAssignOwnerToCompany_ThenReturnSuccess() {
                
        }

        // invariant violation
        @Test
        void GivenTargetAlreadyOwner_WhenAssignOwnerToCompany_ThenReturnFailure() {
                
        }

        // user not found
        @Test
        void GivenCallerUserNotFound_WhenAssignOwnerToCompany_ThenReturnFailure() {
                String token = "token";

                when(mockAuthService.validateToken(token)).thenReturn(true);
                when(mockAuthService.isUserToken(token)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

                when(mockUserRepository.findByID("1"))
                                .thenThrow(new IllegalArgumentException("User not found"));

                Result<Boolean> result = userService.assignOwnerToCompany(1, "2", token);

                assertFalse(result.isSuccess());
        }

        // target user not found
        @Test
        void GivenTargetUserNotFound_WhenAssignOwnerToCompany_ThenReturnFailure() {
                String token = "token";

                when(mockAuthService.validateToken(token)).thenReturn(true);
                when(mockAuthService.isUserToken(token)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

                User mockUser = mock(User.class);

                when(mockUserRepository.findByID("1")).thenReturn(mockUser);
                when(mockUserRepository.findByID("2"))
                                .thenThrow(new IllegalArgumentException("Target not found"));

                Result<Boolean> result = userService.assignOwnerToCompany(1, "2", token);

                assertFalse(result.isSuccess());
        }

        // company not found
        @Test
        void GivenCompanyNotFound_WhenAssignOwnerToCompany_ThenReturnFailure() {
                String token = "token";

                when(mockAuthService.validateToken(token)).thenReturn(true);
                when(mockAuthService.isUserToken(token)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

                when(mockUserRepository.findByID("1")).thenReturn(mock(User.class));
                when(mockUserRepository.findByID("2")).thenReturn(mock(User.class));

                when(mockProductionCompanyRepository.findByID("1"))
                                .thenThrow(new IllegalArgumentException("Company not found"));

                Result<Boolean> result = userService.assignOwnerToCompany(1, "2", token);

                assertFalse(result.isSuccess());
        }

        // auth fail
        @Test
        void GivenInvalidToken_WhenAssignOwnerToCompany_ThenReturnFailure() {
                when(mockAuthService.validateToken("bad")).thenReturn(false);

                Result<Boolean> result = userService.assignOwnerToCompany(1, "2", "bad");

                assertFalse(result.isSuccess());

                verifyNoInteractions(mockUserRepository);
                verifyNoInteractions(mockProductionCompanyRepository);
        }

        // not user token
        @Test
        void GivenNonUserToken_WhenAssignOwnerToCompany_ThenReturnFailure() {
                when(mockAuthService.validateToken("token")).thenReturn(true);
                when(mockAuthService.isUserToken("token")).thenReturn(false);

                Result<Boolean> result = userService.assignOwnerToCompany(1, "2", "token");

                assertFalse(result.isSuccess());

                verifyNoInteractions(mockUserRepository);
                verifyNoInteractions(mockProductionCompanyRepository);
        }

        // save fail
        @Test
        void GivenOptimisticLockFailure_WhenAssignOwnerToCompany_ThenReturnFailure() {
                String token = "token";

                when(mockAuthService.validateToken(token)).thenReturn(true);
                when(mockAuthService.isUserToken(token)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

                User mockUser = mock(User.class);
                User mockTarget = mock(User.class);
                ProductionCompany mockCompany = mock(ProductionCompany.class);

                when(mockUserRepository.findByID("1")).thenReturn(mockUser);
                when(mockUserRepository.findByID("2")).thenReturn(mockTarget);

                when(mockProductionCompanyRepository.findByID("1")).thenReturn(mockCompany);

                doThrow(new OptimisticLockingFailureException("version conflict")).when(mockProductionCompanyRepository)
                                .save(mockCompany);

                Result<Boolean> result = userService.assignOwnerToCompany(1, "2", token);

                assertFalse(result.isSuccess());
        }

        // ---------------------------------------------------------------------
        // ADD MANAGER ASSIGMENT TESTS
        // ----------------------------------------------------------------------

        @Test
        void GivenValidAuthAndExistingUsersAndCompany_WhenAssignManagerToCompany_ThenReturnSuccess() {
                String userID = "1";
                int companyID = 1;
                String targetID = "2";

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
                when(mockUserRepository.findByID(userID)).thenReturn(mockUser);
                when(mockUserRepository.findByID(targetID)).thenReturn(mockTarget);

                // COMPANY
                when(mockProductionCompanyRepository.findByID(String.valueOf(companyID)))
                                .thenReturn(mockCompany);

                doNothing().when(mockCompany).AssignManager(userID, targetID, perms);
                doNothing().when(mockProductionCompanyRepository).save(mockCompany);

                // ACT
                Result<Boolean> result = userService.assignManagerToCompany(companyID, targetID, perms, token);

                // ASSERT
                assertTrue(result.isSuccess());

                // VERIFY
                verify(mockCompany, times(1))
                                .AssignManager(userID, targetID, perms);

                verify(mockProductionCompanyRepository, times(1)).save(mockCompany);

                verify(mockUserRepository).findByID(userID);
                verify(mockUserRepository).findByID(targetID);
        }

        @Test
        void GivenCallerUserNotFound_WhenAssignManagerToCompany_ThenReturnFailure() {
                String token = "token";

                when(mockAuthService.validateToken(token)).thenReturn(true);
                when(mockAuthService.isUserToken(token)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

                when(mockUserRepository.findByID("1"))
                                .thenThrow(new IllegalArgumentException("User not found"));

                Result<Boolean> result = userService.assignManagerToCompany(1, "2", Collections.emptySet(), token);

                assertFalse(result.isSuccess());
        }

        @Test
        void GivenTargetUserNotFound_WhenAssignManagerToCompany_ThenReturnFailure() {
                String token = "token";

                when(mockAuthService.validateToken(token)).thenReturn(true);
                when(mockAuthService.isUserToken(token)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

                User mockUser = mock(User.class);

                when(mockUserRepository.findByID("1")).thenReturn(mockUser);
                when(mockUserRepository.findByID("2"))
                                .thenThrow(new IllegalArgumentException("Target not found"));

                Result<Boolean> result = userService.assignManagerToCompany(1, "2", Collections.emptySet(), token);

                assertFalse(result.isSuccess());
        }

        @Test
        void GivenCompanyNotFound_WhenAssignManagerToCompany_ThenReturnFailure() {
                String token = "token";

                when(mockAuthService.validateToken(token)).thenReturn(true);
                when(mockAuthService.isUserToken(token)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

                when(mockUserRepository.findByID("1")).thenReturn(mock(User.class));
                when(mockUserRepository.findByID("2")).thenReturn(mock(User.class));

                when(mockProductionCompanyRepository.findByID("1"))
                                .thenThrow(new IllegalArgumentException("Company not found"));

                Result<Boolean> result = userService.assignManagerToCompany(1, "2", Collections.emptySet(), token);

                assertFalse(result.isSuccess());
        }

        @Test
        void GivenDomainRuleViolation_WhenAssignManagerToCompany_ThenReturnFailure() {
                String userID = "1";
                int companyID = 1;
                String targetID = "2";

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
                when(mockUserRepository.findByID(userID)).thenReturn(mockUser);
                when(mockUserRepository.findByID(targetID)).thenReturn(mockTarget);

                // COMPANY
                when(mockProductionCompanyRepository.findByID(String.valueOf(companyID)))
                                .thenReturn(mockCompany);

                // DOMAIN FAILURE
                doThrow(new IllegalArgumentException("Target already manager"))
                                .when(mockCompany)
                                .AssignManager(userID, targetID, perms);

                // ACT
                Result<Boolean> result = userService.assignManagerToCompany(companyID, targetID, perms, token);

                // ASSERT
                assertFalse(result.isSuccess());

                // IMPORTANT: ensure save is NOT called
                verify(mockProductionCompanyRepository, never()).save(mockCompany);
        }

        @Test
        void GivenInvalidToken_WhenAssignManagerToCompany_ThenReturnFailure() {
                when(mockAuthService.validateToken("bad")).thenReturn(false);

                Result<Boolean> result = userService.assignManagerToCompany(1, "2", Collections.emptySet(), "bad");

                assertFalse(result.isSuccess());

                verifyNoInteractions(mockUserRepository);
                verifyNoInteractions(mockProductionCompanyRepository);
        }

        @Test
        void GivenNonUserToken_WhenAssignManagerToCompany_ThenReturnFailure() {
                when(mockAuthService.validateToken("token")).thenReturn(true);
                when(mockAuthService.isUserToken("token")).thenReturn(false);

                Result<Boolean> result = userService.assignManagerToCompany(1, "2", Collections.emptySet(), "token");

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

                when(mockUserRepository.findByID("1")).thenReturn(mockUser);
                when(mockUserRepository.findByID("2")).thenReturn(mockTarget);

                when(mockProductionCompanyRepository.findByID("1")).thenReturn(mockCompany);

                doThrow(new OptimisticLockingFailureException("fail"))
                                .when(mockProductionCompanyRepository).save(mockCompany);

                Result<Boolean> result = userService.assignManagerToCompany(1, "2", perms, token);

                assertFalse(result.isSuccess());
        }

        // ----------------------------------------------------------------------
        // ACCEPT INVITE ASSIGMENT TESTS
        // ----------------------------------------------------------------------

        @Test
        void GivenValidInvite_WhenAcceptInviteToCompany_ThenReturnSuccess() {
                String userID = "1";
                int companyID = 1;
                String assignerID = "2";
                String token = "token";

                User mockUser = mock(User.class);
                User mockAssigner = mock(User.class);
                ProductionCompany mockCompany = mock(ProductionCompany.class);

                when(mockAuthService.validateToken(token)).thenReturn(true);
                when(mockAuthService.isUserToken(token)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(token)).thenReturn(String.valueOf(userID));

                when(mockUserRepository.findByID(userID)).thenReturn(mockUser);
                when(mockUserRepository.findByID(assignerID)).thenReturn(mockAssigner);

                when(mockProductionCompanyRepository.findByID(String.valueOf(companyID)))
                                .thenReturn(mockCompany);

                doNothing().when(mockCompany).acceptInvite(userID, assignerID);
                doNothing().when(mockProductionCompanyRepository).save(mockCompany);

                Result<Boolean> result = userService.acceptInviteToCompany(companyID, assignerID, token);

                assertTrue(result.isSuccess());

                verify(mockCompany).acceptInvite(userID, assignerID);
                verify(mockProductionCompanyRepository).save(mockCompany);
        }

        @Test
        void GivenInvalidToken_WhenAcceptInviteToCompany_ThenReturnFail() {
                when(mockAuthService.validateToken("bad")).thenReturn(false);

                Result<Boolean> result = userService.acceptInviteToCompany(1, "2", "bad");

                assertFalse(result.isSuccess());

                verifyNoInteractions(mockUserRepository);
                verifyNoInteractions(mockProductionCompanyRepository);
        }

        @Test
        void GivenNonUserToken_WhenAcceptInviteToCompany_ThenReturnFail() {
                when(mockAuthService.validateToken("token")).thenReturn(true);
                when(mockAuthService.isUserToken("token")).thenReturn(false);

                Result<Boolean> result = userService.acceptInviteToCompany(1, "2", "token");

                assertFalse(result.isSuccess());
        }

        @Test
        void GivenMissingUser_WhenAcceptInviteToCompany_ThenReturnFail() {
                String token = "token";

                when(mockAuthService.validateToken(token)).thenReturn(true);
                when(mockAuthService.isUserToken(token)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

                when(mockUserRepository.findByID("1"))
                                .thenThrow(new IllegalArgumentException("User not found"));

                Result<Boolean> result = userService.acceptInviteToCompany(1, "2", token);

                assertFalse(result.isSuccess());
        }

        @Test
        void GivenMissingAssigner_WhenAcceptInviteToCompany_ThenReturnFail() {
                String token = "token";

                when(mockAuthService.validateToken(token)).thenReturn(true);
                when(mockAuthService.isUserToken(token)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

                User mockUser = mock(User.class);

                when(mockUserRepository.findByID("1")).thenReturn(mockUser);
                when(mockUserRepository.findByID("2"))
                                .thenThrow(new IllegalArgumentException("Assigner not found"));

                Result<Boolean> result = userService.acceptInviteToCompany(1, "2", token);

                assertFalse(result.isSuccess());
        }

        @Test
        void GivenMissingCompany_WhenAcceptInviteToCompany_ThenReturnFail() {
                String token = "token";

                when(mockAuthService.validateToken(token)).thenReturn(true);
                when(mockAuthService.isUserToken(token)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

                when(mockUserRepository.findByID("1")).thenReturn(mock(User.class));
                when(mockUserRepository.findByID("2")).thenReturn(mock(User.class));

                when(mockProductionCompanyRepository.findByID("1"))
                                .thenThrow(new IllegalArgumentException("Company not found"));

                Result<Boolean> result = userService.acceptInviteToCompany(1, "2", token);

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

                when(mockUserRepository.findByID("1")).thenReturn(mockUser);
                when(mockUserRepository.findByID("2")).thenReturn(mockAssigner);

                when(mockProductionCompanyRepository.findByID("1"))
                                .thenReturn(mockCompany);

                doThrow(new IllegalArgumentException("Invite not found"))
                                .when(mockCompany).acceptInvite("1", "2");

                Result<Boolean> result = userService.acceptInviteToCompany(1, "2", token);

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

                when(mockUserRepository.findByID("1")).thenReturn(mockUser);
                when(mockUserRepository.findByID("2")).thenReturn(mockAssigner);

                when(mockProductionCompanyRepository.findByID("1"))
                                .thenReturn(mockCompany);

                doThrow(new OptimisticLockingFailureException("conflict"))
                                .when(mockProductionCompanyRepository).save(mockCompany);

                Result<Boolean> result = userService.acceptInviteToCompany(1, "2", token);

                assertFalse(result.isSuccess());
        }

        // ----------------------------------------------------------------------
        // REJECT INVITE ASSIGMENT TESTS
        // ----------------------------------------------------------------------
        @Test
        void GivenValidInvite_WhenRejectInviteToCompany_ThenReturnSuccess() {
                String userID = "1";
                int companyID = 1;
                String assignerID = "2";
                String token = "token";

                User mockUser = mock(User.class);
                User mockAssigner = mock(User.class);
                ProductionCompany mockCompany = mock(ProductionCompany.class);

                when(mockAuthService.validateToken(token)).thenReturn(true);
                when(mockAuthService.isUserToken(token)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(token)).thenReturn(String.valueOf(userID));

                when(mockUserRepository.findByID(userID)).thenReturn(mockUser);
                when(mockUserRepository.findByID(assignerID)).thenReturn(mockAssigner);

                when(mockProductionCompanyRepository.findByID(String.valueOf(companyID)))
                                .thenReturn(mockCompany);

                doNothing().when(mockCompany).rejectInvite(userID, assignerID);
                doNothing().when(mockProductionCompanyRepository).save(mockCompany);

                Result<Boolean> result = userService.rejectInviteToCompany(companyID, assignerID, token);

                assertTrue(result.isSuccess());

                verify(mockCompany).rejectInvite(userID, assignerID);
                verify(mockProductionCompanyRepository).save(mockCompany);
        }

        @Test
        void GivenInvalidToken_WhenRejectInviteToCompany_ThenReturnFail() {
                when(mockAuthService.validateToken("bad")).thenReturn(false);

                Result<Boolean> result = userService.rejectInviteToCompany(1, "2", "bad");

                assertFalse(result.isSuccess());

                verifyNoInteractions(mockUserRepository);
                verifyNoInteractions(mockProductionCompanyRepository);
        }

        @Test
        void GivenNonUserToken_WhenRejectInviteToCompany_ThenReturnFail() {
                when(mockAuthService.validateToken("token")).thenReturn(true);
                when(mockAuthService.isUserToken("token")).thenReturn(false);

                Result<Boolean> result = userService.rejectInviteToCompany(1, "2", "token");

                assertFalse(result.isSuccess());
        }

        @Test
        void GivenMissingUser_WhenRejectInviteToCompany_ThenReturnFail() {
                String token = "token";

                when(mockAuthService.validateToken(token)).thenReturn(true);
                when(mockAuthService.isUserToken(token)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

                when(mockUserRepository.findByID("1"))
                                .thenThrow(new IllegalArgumentException("User not found"));

                Result<Boolean> result = userService.rejectInviteToCompany(1, "2", token);

                assertFalse(result.isSuccess());
        }

        @Test
        void GivenMissingAssigner_WhenRejectInviteToCompany_ThenReturnFail() {
                String token = "token";

                when(mockAuthService.validateToken(token)).thenReturn(true);
                when(mockAuthService.isUserToken(token)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

                User mockUser = mock(User.class);

                when(mockUserRepository.findByID("1")).thenReturn(mockUser);
                when(mockUserRepository.findByID("2"))
                                .thenThrow(new IllegalArgumentException("Assigner not found"));

                Result<Boolean> result = userService.rejectInviteToCompany(1, "2", token);

                assertFalse(result.isSuccess());
        }

        @Test
        void GivenMissingCompany_WhenRejectInviteToCompany_ThenReturnFail() {
                String token = "token";

                when(mockAuthService.validateToken(token)).thenReturn(true);
                when(mockAuthService.isUserToken(token)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(token)).thenReturn("1");

                when(mockUserRepository.findByID("1")).thenReturn(mock(User.class));
                when(mockUserRepository.findByID("2")).thenReturn(mock(User.class));

                when(mockProductionCompanyRepository.findByID("1"))
                                .thenThrow(new IllegalArgumentException("Company not found"));

                Result<Boolean> result = userService.rejectInviteToCompany(1, "2", token);

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

                when(mockUserRepository.findByID("1")).thenReturn(mockUser);
                when(mockUserRepository.findByID("2")).thenReturn(mockAssigner);

                when(mockProductionCompanyRepository.findByID("1"))
                                .thenReturn(mockCompany);

                doThrow(new IllegalArgumentException("Invite not found"))
                                .when(mockCompany).rejectInvite("1", "2");

                Result<Boolean> result = userService.rejectInviteToCompany(1, "2", token);

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

                when(mockUserRepository.findByID("1")).thenReturn(mockUser);
                when(mockUserRepository.findByID("2")).thenReturn(mockAssigner);

                when(mockProductionCompanyRepository.findByID("1"))
                                .thenReturn(mockCompany);

                doThrow(new OptimisticLockingFailureException("conflict"))
                                .when(mockProductionCompanyRepository).save(mockCompany);

                Result<Boolean> result = userService.rejectInviteToCompany(1, "2", token);

                assertFalse(result.isSuccess());
        }

        // -----------------------------------------------------------------
        // FORFEIT OWNERSHIP TESTS
        // -----------------------------------------------------------------
        @Test
        void GivenValidOwner_WhenForfeitOwnership_ThenReturnSuccess() {
                String userID = "1";
                int companyID = 1;
                String token = "token";

                User mockUser = mock(User.class);
                ProductionCompany mockCompany = mock(ProductionCompany.class);

                when(mockAuthService.validateToken(token)).thenReturn(true);
                when(mockAuthService.isUserToken(token)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(token)).thenReturn(String.valueOf(userID));

                when(mockUserRepository.findByID(userID)).thenReturn(mockUser);
                when(mockProductionCompanyRepository.findByID(String.valueOf(companyID)))
                                .thenReturn(mockCompany);

                doNothing().when(mockCompany).forfeitOwnership(userID);
                doNothing().when(mockProductionCompanyRepository).save(mockCompany);

                Result<Boolean> result = userService.forfeitOwnership(companyID, token);

                assertTrue(result.isSuccess());

                verify(mockCompany).forfeitOwnership(userID);
                verify(mockProductionCompanyRepository).save(mockCompany);
        }

        @Test
        void GivenUserNotOwner_WhenForfeitOwnership_ThenReturnFailure() {
                String userID = "1";
                int companyID = 1;
                String token = "token";

                User mockUser = mock(User.class);
                ProductionCompany mockCompany = mock(ProductionCompany.class);

                when(mockAuthService.validateToken(token)).thenReturn(true);
                when(mockAuthService.isUserToken(token)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(token)).thenReturn(String.valueOf(userID));

                when(mockUserRepository.findByID(userID)).thenReturn(mockUser);
                when(mockProductionCompanyRepository.findByID(String.valueOf(companyID)))
                                .thenReturn(mockCompany);

                doThrow(new IllegalArgumentException("User not owner"))
                                .when(mockCompany).forfeitOwnership(userID);

                Result<Boolean> result = userService.forfeitOwnership(companyID, token);

                assertFalse(result.isSuccess());

                verify(mockProductionCompanyRepository, never()).save(any());
        }

        @Test
        void GivenMissingUser_WhenForfeitOwnership_ThenReturnFailure() {
                String userID = "1";
                int companyID = 1;
                String token = "token";

                when(mockAuthService.validateToken(token)).thenReturn(true);
                when(mockAuthService.isUserToken(token)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(token)).thenReturn(String.valueOf(userID));

                when(mockUserRepository.findByID(userID))
                                .thenThrow(new IllegalArgumentException("User not found"));

                Result<Boolean> result = userService.forfeitOwnership(companyID, token);

                assertFalse(result.isSuccess());

                verifyNoInteractions(mockProductionCompanyRepository);
        }

        @Test
        void GivenMissingCompany_WhenForfeitOwnership_ThenReturnFailure() {
                String userID = "1";
                int companyID = 1;
                String token = "token";

                User mockUser = mock(User.class);

                when(mockAuthService.validateToken(token)).thenReturn(true);
                when(mockAuthService.isUserToken(token)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(token)).thenReturn(String.valueOf(userID));

                when(mockUserRepository.findByID(userID)).thenReturn(mockUser);

                when(mockProductionCompanyRepository.findByID(String.valueOf(companyID)))
                                .thenThrow(new IllegalArgumentException("Company not found"));

                Result<Boolean> result = userService.forfeitOwnership(companyID, token);

                assertFalse(result.isSuccess());

                verify(mockProductionCompanyRepository, never()).save(any());
        }

        @Test
        void GivenInvalidToken_WhenForfeitOwnership_ThenReturnFailure() {
                when(mockAuthService.validateToken("bad")).thenReturn(false);

                Result<Boolean> result = userService.forfeitOwnership(1, "bad");

                assertFalse(result.isSuccess());

                verifyNoInteractions(mockUserRepository);
                verifyNoInteractions(mockProductionCompanyRepository);
        }

        // ------------------------------------------------------------------------------------------
        // REMOVE PERSONAL FROM COMPANY TESTS
        // ------------------------------------------------------------------------------------------

        @Test
        void GivenValidRequest_WhenRemoveOwnerManager_ThenReturnSuccess() {
                String userID = "1";
                int companyID = 1;
                String targetID = "2";

                User mockUser = mock(User.class);
                User mockTarget = mock(User.class);
                ProductionCompany mockCompany = mock(ProductionCompany.class);

                when(mockAuthService.validateToken(anyString())).thenReturn(true);
                when(mockAuthService.isUserToken(anyString())).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));

                when(mockUserRepository.findByID(userID)).thenReturn(mockUser);
                when(mockUserRepository.findByID(targetID)).thenReturn(mockTarget);

                when(mockProductionCompanyRepository.findByID(String.valueOf(companyID))).thenReturn(mockCompany);

                doNothing().when(mockCompany).removeMemberByOwner(userID, targetID);
                doNothing().when(mockProductionCompanyRepository).save(mockCompany);

                assertTrue(userService.removeOwnerManager(targetID, companyID, "token").isSuccess());
        }

        @Test
        void GivenInvalidToken_WhenRemoveOwnerManager_ThenReturnFail() {
                when(mockAuthService.validateToken(anyString())).thenReturn(false);

                assertFalse(userService.removeOwnerManager("2", 1, "token").isSuccess());
        }

        @Test
        void GivenNonUserToken_WhenRemoveOwnerManager_ThenReturnFail() {
                when(mockAuthService.validateToken(anyString())).thenReturn(true);
                when(mockAuthService.isUserToken(anyString())).thenReturn(false);

                assertFalse(userService.removeOwnerManager("2", 1, "token").isSuccess());
        }

        @Test
        void GivenUserNotFound_WhenRemoveOwnerManager_ThenReturnFail() {
                when(mockAuthService.validateToken(anyString())).thenReturn(true);
                when(mockAuthService.isUserToken(anyString())).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn("1");

                when(mockUserRepository.findByID(anyString()))
                                .thenThrow(new IllegalArgumentException("User not found"));

                assertFalse(userService.removeOwnerManager("2", 1, "token").isSuccess());
        }

        @Test
        void GivenTargetUserNotFound_WhenRemoveOwnerManager_ThenReturnFail() {
                String userID = "1";

                when(mockAuthService.validateToken(anyString())).thenReturn(true);
                when(mockAuthService.isUserToken(anyString())).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));

                when(mockUserRepository.findByID(userID)).thenReturn(mock(User.class));
                when(mockUserRepository.findByID("2")).thenThrow(new IllegalArgumentException("Target not found"));

                assertFalse(userService.removeOwnerManager("2", 1, "token").isSuccess());
        }

        @Test
        void GivenCompanyNotFound_WhenRemoveOwnerManager_ThenReturnFail() {
                int userID = 1;

                when(mockAuthService.validateToken(anyString())).thenReturn(true);
                when(mockAuthService.isUserToken(anyString())).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));

                when(mockUserRepository.findByID(anyString())).thenReturn(mock(User.class));
                when(mockProductionCompanyRepository.findByID(anyString()))
                                .thenThrow(new IllegalArgumentException("Company not found"));

                assertFalse(userService.removeOwnerManager("2", 1, "token").isSuccess());
        }

        @Test
        void GivenRemoveMemberThrowsException_WhenRemoveOwnerManager_ThenReturnFail() {
                String userID = "1";

                User mockUser = mock(User.class);
                User mockTarget = mock(User.class);
                ProductionCompany mockCompany = mock(ProductionCompany.class);

                when(mockAuthService.validateToken(anyString())).thenReturn(true);
                when(mockAuthService.isUserToken(anyString())).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));

                when(mockUserRepository.findByID(userID)).thenReturn(mockUser);
                when(mockUserRepository.findByID("2")).thenReturn(mockTarget);

                when(mockProductionCompanyRepository.findByID(anyString())).thenReturn(mockCompany);

                doThrow(new IllegalArgumentException("Not allowed"))
                                .when(mockCompany).removeMemberByOwner(userID, "2");

                assertFalse(userService.removeOwnerManager("2", 1, "token").isSuccess());
        }

        @Test
        void GivenSaveThrowsOptimisticLocking_WhenRemoveOwnerManager_ThenReturnFail() {
                String userID = "1";

                User mockUser = mock(User.class);
                User mockTarget = mock(User.class);
                ProductionCompany mockCompany = mock(ProductionCompany.class);

                when(mockAuthService.validateToken(anyString())).thenReturn(true);
                when(mockAuthService.isUserToken(anyString())).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(anyString())).thenReturn(String.valueOf(userID));

                when(mockUserRepository.findByID(userID)).thenReturn(mockUser);
                when(mockUserRepository.findByID("2")).thenReturn(mockTarget);

                when(mockProductionCompanyRepository.findByID(anyString())).thenReturn(mockCompany);

                doNothing().when(mockCompany).removeMemberByOwner(userID, "2");
                doThrow(new OptimisticLockingFailureException("conflict"))
                                .when(mockProductionCompanyRepository).save(mockCompany);

                assertFalse(userService.removeOwnerManager("2", 1, "token").isSuccess());
        }

        @Test
        void GivenJwtException_WhenRemoveOwnerManager_ThenReturnFail() {
                when(mockAuthService.validateToken(anyString()))
                                .thenThrow(new JwtException("bad token"));

                assertFalse(userService.removeOwnerManager("2", 1, "token").isSuccess());
        }

        @Test
        void GivenUnexpectedException_WhenRemoveOwnerManager_ThenReturnFail() {
                when(mockAuthService.validateToken(anyString()))
                                .thenThrow(new RuntimeException("unexpected"));

                assertFalse(userService.removeOwnerManager("2", 1, "token").isSuccess());
        }

        @Test
        void GivenValidRequest_WhenChangeManagerPermission_ThenReturnSuccess() {
                String userID = "1";
                String targetID = "2";
                int companyID = 10;

                Set<ManagerPermissions> permissions = Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

                User mockUser = mock(User.class);
                User mockTarget = mock(User.class);
                ProductionCompany mockCompany = mock(ProductionCompany.class);

                when(mockAuthService.validateToken(anyString())).thenReturn(true);
                when(mockAuthService.isUserToken(anyString())).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(anyString()))
                                .thenReturn(String.valueOf(userID));

                when(mockUserRepository.findByID(userID)).thenReturn(mockUser);
                when(mockUserRepository.findByID(targetID)).thenReturn(mockTarget);

                when(mockProductionCompanyRepository.findByID(String.valueOf(companyID))).thenReturn(mockCompany);

                doNothing().when(mockCompany)
                                .updatePermissionsOfManager(userID, targetID, permissions);

                doNothing().when(mockProductionCompanyRepository).save(mockCompany);

                assertTrue(userService.changeManagerPermission(
                                targetID,
                                companyID,
                                permissions,
                                "token").isSuccess());
        }

        @Test
        void GivenInvalidToken_WhenChangeManagerPermission_ThenReturnFail() {
                Set<ManagerPermissions> permissions = Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

                when(mockAuthService.validateToken(anyString())).thenReturn(false);

                assertFalse(userService.changeManagerPermission(
                                "2",
                                1,
                                permissions,
                                "token").isSuccess());
        }

        @Test
        void GivenNonUserToken_WhenChangeManagerPermission_ThenReturnFail() {
                Set<ManagerPermissions> permissions = Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

                when(mockAuthService.validateToken(anyString())).thenReturn(true);
                when(mockAuthService.isUserToken(anyString())).thenReturn(false);

                assertFalse(userService.changeManagerPermission(
                                "2",
                                1,
                                permissions,
                                "token").isSuccess());
        }

        @Test
        void GivenUserNotFound_WhenChangeManagerPermission_ThenReturnFail() {
                Set<ManagerPermissions> permissions = Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

                when(mockAuthService.validateToken(anyString())).thenReturn(true);
                when(mockAuthService.isUserToken(anyString())).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(anyString()))
                                .thenReturn("1");

                when(mockUserRepository.findByID(anyString()))
                                .thenThrow(new IllegalArgumentException("User not found"));

                assertFalse(userService.changeManagerPermission(
                                "2",
                                1,
                                permissions,
                                "token").isSuccess());
        }

        @Test
        void GivenTargetUserNotFound_WhenChangeManagerPermission_ThenReturnFail() {
                String userID = "1";

                Set<ManagerPermissions> permissions = Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

                when(mockAuthService.validateToken(anyString())).thenReturn(true);
                when(mockAuthService.isUserToken(anyString())).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(anyString()))
                                .thenReturn(String.valueOf(userID));

                when(mockUserRepository.findByID(userID))
                                .thenReturn(mock(User.class));

                when(mockUserRepository.findByID("2"))
                                .thenThrow(new IllegalArgumentException("Target not found"));

                assertFalse(userService.changeManagerPermission(
                                "2",
                                1,
                                permissions,
                                "token").isSuccess());
        }

        @Test
        void GivenCompanyNotFound_WhenChangeManagerPermission_ThenReturnFail() {
                int userID = 1;

                Set<ManagerPermissions> permissions = Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

                when(mockAuthService.validateToken(anyString())).thenReturn(true);
                when(mockAuthService.isUserToken(anyString())).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(anyString()))
                                .thenReturn(String.valueOf(userID));

                when(mockUserRepository.findByID(anyString()))
                                .thenReturn(mock(User.class));

                when(mockProductionCompanyRepository.findByID(anyString()))
                                .thenThrow(new IllegalArgumentException("Company not found"));

                assertFalse(userService.changeManagerPermission(
                                "2",
                                1,
                                permissions,
                                "token").isSuccess());
        }

        @Test
        void GivenUpdatePermissionsThrowsException_WhenChangeManagerPermission_ThenReturnFail() {
                String userID = "1";

                Set<ManagerPermissions> permissions = Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

                User mockUser = mock(User.class);
                User mockTarget = mock(User.class);
                ProductionCompany mockCompany = mock(ProductionCompany.class);

                when(mockAuthService.validateToken(anyString())).thenReturn(true);
                when(mockAuthService.isUserToken(anyString())).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(anyString()))
                                .thenReturn(String.valueOf(userID));

                when(mockUserRepository.findByID(userID)).thenReturn(mockUser);
                when(mockUserRepository.findByID("2")).thenReturn(mockTarget);

                when(mockProductionCompanyRepository.findByID(anyString()))
                                .thenReturn(mockCompany);

                doThrow(new IllegalArgumentException("Not allowed"))
                                .when(mockCompany)
                                .updatePermissionsOfManager(userID, "2", permissions);

                assertFalse(userService.changeManagerPermission(
                                "2",
                                1,
                                permissions,
                                "token").isSuccess());
        }

        @Test
        void GivenSaveThrowsOptimisticLocking_WhenChangeManagerPermission_ThenReturnFail() {
                String userID = "1";

                Set<ManagerPermissions> permissions = Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

                User mockUser = mock(User.class);
                User mockTarget = mock(User.class);
                ProductionCompany mockCompany = mock(ProductionCompany.class);

                when(mockAuthService.validateToken(anyString())).thenReturn(true);
                when(mockAuthService.isUserToken(anyString())).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(anyString()))
                                .thenReturn(String.valueOf(userID));

                when(mockUserRepository.findByID(userID)).thenReturn(mockUser);
                when(mockUserRepository.findByID("2")).thenReturn(mockTarget);

                when(mockProductionCompanyRepository.findByID(anyString()))
                                .thenReturn(mockCompany);

                doNothing().when(mockCompany)
                                .updatePermissionsOfManager(userID, "2", permissions);

                doThrow(new OptimisticLockingFailureException("conflict"))
                                .when(mockProductionCompanyRepository)
                                .save(mockCompany);

                assertFalse(userService.changeManagerPermission(
                                "2",
                                1,
                                permissions,
                                "token").isSuccess());
        }

        @Test
        void GivenJwtException_WhenChangeManagerPermission_ThenReturnFail() {
                Set<ManagerPermissions> permissions = Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

                when(mockAuthService.validateToken(anyString()))
                                .thenThrow(new JwtException("bad token"));

                assertFalse(userService.changeManagerPermission(
                                "2",
                                1,
                                permissions,
                                "token").isSuccess());
        }

        @Test
        void GivenUnexpectedException_WhenChangeManagerPermission_ThenReturnFail() {
                Set<ManagerPermissions> permissions = Set.of(ManagerPermissions.CUSTOMER_SUPPORT);

                when(mockAuthService.validateToken(anyString()))
                                .thenThrow(new RuntimeException("unexpected"));

                assertFalse(userService.changeManagerPermission(
                                "2",
                                1,
                                permissions,
                                "token").isSuccess());
        }

        @Test
        void GivenValidRequest_WhenHierarchyTree_ThenReturnSuccess() {
                String userID = "1";
                int companyID = 10;

                ProductionCompany mockCompany = mock(ProductionCompany.class);
                User mockUser = mock(User.class);

                List<HierarchyNodeData> hierarchyData = List.of(
                                new HierarchyNodeData(
                                                "1",
                                                null,
                                                RoleType.OWNER,
                                                Set.of()),
                                new HierarchyNodeData(
                                                "2",
                                                "1",
                                                RoleType.MANAGER,
                                                Set.of(ManagerPermissions.CUSTOMER_SUPPORT)));

                when(mockAuthService.validateToken(anyString())).thenReturn(true);
                when(mockAuthService.isUserToken(anyString())).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(anyString()))
                                .thenReturn(String.valueOf(userID));

                when(mockUserRepository.findByID(userID)).thenReturn(mockUser);

                when(mockProductionCompanyRepository.findByID(String.valueOf(companyID)))
                                .thenReturn(mockCompany);

                when(mockCompany.getHierarchyTree(userID))
                                .thenReturn(hierarchyData);

                Result<List<HierarchyNodeDTO>> result = userService.hierarchyTree(companyID, "token");

                assertTrue(result.isSuccess());
                assertEquals(2, result.getValue().size());
        }

        @Test
        void GivenInvalidToken_WhenHierarchyTree_ThenReturnFail() {
                when(mockAuthService.validateToken(anyString())).thenReturn(false);

                assertFalse(userService.hierarchyTree(1, "token").isSuccess());
        }

        @Test
        void GivenNonUserToken_WhenHierarchyTree_ThenReturnFail() {
                when(mockAuthService.validateToken(anyString())).thenReturn(true);
                when(mockAuthService.isUserToken(anyString())).thenReturn(false);

                assertFalse(userService.hierarchyTree(1, "token").isSuccess());
        }

        @Test
        void GivenUserNotFound_WhenHierarchyTree_ThenReturnFail() {
                when(mockAuthService.validateToken(anyString())).thenReturn(true);
                when(mockAuthService.isUserToken(anyString())).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(anyString()))
                                .thenReturn("1");

                when(mockUserRepository.findByID("1"))
                                .thenThrow(new IllegalArgumentException("User not found"));

                assertFalse(userService.hierarchyTree(1, "token").isSuccess());
        }

        @Test
        void GivenCompanyNotFound_WhenHierarchyTree_ThenReturnFail() {
                String userID = "1";

                when(mockAuthService.validateToken(anyString())).thenReturn(true);
                when(mockAuthService.isUserToken(anyString())).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(anyString()))
                                .thenReturn(String.valueOf(userID));

                when(mockUserRepository.findByID(userID))
                                .thenReturn(mock(User.class));

                when(mockProductionCompanyRepository.findByID(anyString()))
                                .thenThrow(new IllegalArgumentException("Company not found"));

                assertFalse(userService.hierarchyTree(1, "token").isSuccess());
        }

        @Test
        void GivenHierarchyTreeThrowsException_WhenHierarchyTree_ThenReturnFail() {
                String userID = "1";
                int companyID = 10;

                ProductionCompany mockCompany = mock(ProductionCompany.class);

                when(mockAuthService.validateToken(anyString())).thenReturn(true);
                when(mockAuthService.isUserToken(anyString())).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(anyString()))
                                .thenReturn(String.valueOf(userID));

                when(mockUserRepository.findByID(userID))
                                .thenReturn(mock(User.class));

                when(mockProductionCompanyRepository.findByID(String.valueOf(companyID)))
                                .thenReturn(mockCompany);

                when(mockCompany.getHierarchyTree(userID))
                                .thenThrow(new IllegalArgumentException("Not owner"));

                assertFalse(userService.hierarchyTree(companyID, "token").isSuccess());
        }

        @Test
        void GivenJwtException_WhenHierarchyTree_ThenReturnFail() {
                when(mockAuthService.validateToken(anyString()))
                                .thenThrow(new JwtException("bad token"));

                assertFalse(userService.hierarchyTree(1, "token").isSuccess());
        }

        @Test
        void GivenUnexpectedException_WhenHierarchyTree_ThenReturnFail() {
                when(mockAuthService.validateToken(anyString()))
                                .thenThrow(new RuntimeException("unexpected"));

                assertFalse(userService.hierarchyTree(1, "token").isSuccess());
        }

}