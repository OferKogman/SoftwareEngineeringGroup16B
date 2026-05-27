package com.group16b.ApplicationLayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.ApplicationLayer.DTOs.HierarchyNodeDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.User.User;
import com.group16b.InfrastructureLayer.MapDBs.ProductionCompanyRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;

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
        private final String STALE_USER_TOKEN="stale-user-token";

        private ProductionCompany company1;
        private ProductionCompany company2;

        private final int COMPANY1_ID=1;
        private final int COMPANY2_ID=2;
        private final int BAD_COMPANY_ID=999;

        private final Set<ManagerPermissions> ALL_MANAGER_PERMISSIONS = EnumSet.allOf(ManagerPermissions.class);
        private final Set<ManagerPermissions> NEW_MANAGER_PERMISSIONS = EnumSet.of(ManagerPermissions.CUSTOMER_SUPPORT);

        private Set<String> OWNER2_DEFAULT_CHILDREN;
        private Set<String> OWNER1_DEFAULT_CHILDREN;

        @BeforeEach
        void setUp() {
                mockAuthService = mock(IAuthenticationService.class);
                UserRepository = new UserRepositoryMapImpl();
                ProductionCompanyRepository = new ProductionCompanyRepositoryMapImpl();
                CompanyHierarchyService = new CompanyHierarchyService(mockAuthService, ProductionCompanyRepository, UserRepository);

                when(mockAuthService.validateToken(anyString())).thenReturn(false);
                when(mockAuthService.isUserToken(anyString())).thenReturn(false);

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
                
                when(mockAuthService.validateToken(STALE_USER_TOKEN)).thenReturn(true);
                when(mockAuthService.isUserToken(STALE_USER_TOKEN)).thenReturn(true);
                when(mockAuthService.extractSubjectFromToken(STALE_USER_TOKEN)).thenReturn(BAD_USER_EMAIL);

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
                assignManager(company1, OWNER1_EMAIL, MANAGER1_EMAIL, ALL_MANAGER_PERMISSIONS);
                assignManager(company1, OWNER2_EMAIL, MANAGER2_EMAIL, ALL_MANAGER_PERMISSIONS);

                company1.AssignManager(OWNER1_EMAIL, INVITED_MANAGER_EMAIL, ALL_MANAGER_PERMISSIONS);
                company1.AssignOwner(OWNER2_EMAIL,INVITED_OWNER_EMAIL);
                company1.AssignManager(OWNER1_EMAIL, INVITED_OWNER_EMAIL, ALL_MANAGER_PERMISSIONS);
                company1.AssignOwner(OWNER1_EMAIL, MANAGER2_EMAIL);

                ProductionCompanyRepository.save(company1);
                ProductionCompanyRepository.save(company2);
                
                OWNER2_DEFAULT_CHILDREN =new HashSet<>(Set.of(MANAGER2_EMAIL));

                OWNER1_DEFAULT_CHILDREN =new HashSet<>(Set.of(OWNER2_EMAIL,MANAGER1_EMAIL));
        }

        private void assignManager(ProductionCompany company, String assignerEmail, String managerEmail, Set<ManagerPermissions> perms)
        {
                company.AssignManager(assignerEmail, managerEmail, perms);
                company.acceptInvite(managerEmail, assignerEmail);
        }
        private void assignOwner(ProductionCompany company, String assignerEmail, String newOwnerEmail)
        {
                company.AssignOwner(assignerEmail, newOwnerEmail);
                company.acceptInvite(newOwnerEmail, assignerEmail);
        }

                //checks that user didnt have an invite and wasnt invited
        private void verifyUserDidntGetInvite(String userEmail)
        {
                assertFalse(company1.hasPendingInvite(userEmail));
                ProductionCompany updated =ProductionCompanyRepository.findByID(String.valueOf(COMPANY1_ID));
                assertFalse(updated.hasPendingInvite(userEmail));
        }

        private void children_didnt_change()
        {
                ProductionCompany updated =ProductionCompanyRepository.findByID(String.valueOf(COMPANY1_ID));
                assertTrue(updated.areDirectSubordinates(OWNER1_EMAIL, OWNER1_DEFAULT_CHILDREN));
                assertTrue(updated.areDirectSubordinates(OWNER2_EMAIL, OWNER2_DEFAULT_CHILDREN));
        }


        // good
        @Test
        void assignOwnerToCompany_success() {
                assertFalse(company1.hasPendingInvite(BYSTANDER_EMAIL));
                Result<Boolean> result =
                        CompanyHierarchyService.assignOwnerToCompany(
                        COMPANY1_ID,
                        BYSTANDER_EMAIL,
                        VALID_OWNER1_TOKEN
                        );

                assertTrue(result.isSuccess());
                assertTrue(result.getValue());

                ProductionCompany updated =ProductionCompanyRepository.findByID(String.valueOf(COMPANY1_ID));
                assertTrue(updated.hasPendingOwnerInvite(BYSTANDER_EMAIL, OWNER1_EMAIL));
        }

        @Test
        void assignOwnerToCompany_invalidToken_fails() {
                Result<Boolean> result =
                        CompanyHierarchyService.assignOwnerToCompany(
                        COMPANY1_ID,
                        BYSTANDER_EMAIL,
                        INVALID_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals("Invalid Token", result.getError());

                verifyUserDidntGetInvite(BYSTANDER_EMAIL);
        }

        @Test
        void assignOwnerToCompany_staleUser_fails() {
                Result<Boolean> result =
                        CompanyHierarchyService.assignOwnerToCompany(
                        COMPANY1_ID,
                        BYSTANDER_EMAIL,
                        STALE_USER_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals(
                "User with ID " + BAD_USER_EMAIL + " not found.",
                result.getError());
                
                verifyUserDidntGetInvite(BYSTANDER_EMAIL);
        }

        @Test
        void assignOwnerToCompany_managerCannotAssignOwner() {
                Result<Boolean> result =
                        CompanyHierarchyService.assignOwnerToCompany(
                        COMPANY1_ID,
                        BYSTANDER_EMAIL,
                        VALID_MANAGER1_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("caller User is not owner in Assign Owner"));
                verifyUserDidntGetInvite(BYSTANDER_EMAIL);
        }
        @Test
        void assignOwnerToCompany_bystanderCannotAssignOwner() {
                Result<Boolean> result =
                        CompanyHierarchyService.assignOwnerToCompany(
                        COMPANY1_ID,
                        BYSTANDER_EMAIL,
                        VALID_BYSTANDER_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("caller User is not owner in Assign Owner"));
                verifyUserDidntGetInvite(BYSTANDER_EMAIL);
        }

        @Test
        void assignOwnerToCompany_targetUserNotFound() {
                Result<Boolean> result =
                        CompanyHierarchyService.assignOwnerToCompany(
                        COMPANY1_ID,
                        BAD_USER_EMAIL,
                        VALID_OWNER1_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals("User with ID " + BAD_USER_EMAIL + " not found.", result.getError());
                verifyUserDidntGetInvite(BAD_USER_EMAIL);
        }

        @Test
        void assignOwnerToCompany_companyNotFound() {
                Result<Boolean> result =
                        CompanyHierarchyService.assignOwnerToCompany(
                        BAD_COMPANY_ID,
                        BYSTANDER_EMAIL,
                        VALID_OWNER1_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals("Production company with ID "+BAD_COMPANY_ID+" is not found.", result.getError());
        }

        @Test
        void assignOwnerToCompany_existingOwner_fails() {
        Result<Boolean> result =
                CompanyHierarchyService.assignOwnerToCompany(
                COMPANY1_ID,
                OWNER2_EMAIL,
                VALID_OWNER1_TOKEN
                );

        assertFalse(result.isSuccess());
        assertEquals("Target " + OWNER2_EMAIL + " is already an owner of the company.", result.getError());
        verifyUserDidntGetInvite(OWNER2_EMAIL);
        }

        @Test
        void assignOwnerToCompany_duplicatePendingInvite_allowed() {
                assertTrue(company1.hasPendingInvite(INVITED_OWNER_EMAIL));
                Result<Boolean> result =
                        CompanyHierarchyService.assignOwnerToCompany(
                        COMPANY1_ID,
                        INVITED_OWNER_EMAIL,
                        VALID_OWNER2_TOKEN
                        );

                assertTrue(result.isSuccess());
                ProductionCompany updated =ProductionCompanyRepository.findByID(String.valueOf(COMPANY1_ID));
                assertTrue(updated.hasPendingOwnerInvite(INVITED_OWNER_EMAIL, OWNER2_EMAIL));
        }

        @Test
        void assignOwnerToCompany_unexpectedException() {
                IProductionCompanyRepository repo =mock(IProductionCompanyRepository.class);

                when(repo.findByID(anyString()))
                        .thenThrow(new RuntimeException("DB exploded"));

                CompanyHierarchyService service =
                        new CompanyHierarchyService(
                        mockAuthService,
                        repo,
                        UserRepository
                        );

                Result<Boolean> result =
                        service.assignOwnerToCompany(
                        COMPANY1_ID,
                        BYSTANDER_EMAIL,
                        VALID_OWNER1_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertTrue(result.getError().contains("unexpected"));
                verify(repo, never()).save(any());
        }

        @Test
        void concurrentAssignOwner_differentTargets_bothSucceed() throws Exception {

                User userA = new User("a@test.com", "password");
                User userB = new User("b@test.com", "password");

                UserRepository.save(userA);
                UserRepository.save(userB);

                ExecutorService executor = Executors.newFixedThreadPool(2);

                CountDownLatch startLatch = new CountDownLatch(1);

                Callable<Result<Boolean>> task1 = () -> {
                        startLatch.await();

                        return CompanyHierarchyService.assignOwnerToCompany(
                        COMPANY1_ID,
                        "a@test.com",
                        VALID_OWNER1_TOKEN
                        );
                };

                Callable<Result<Boolean>> task2 = () -> {
                        startLatch.await();

                        return CompanyHierarchyService.assignOwnerToCompany(
                        COMPANY1_ID,
                        "b@test.com",
                        VALID_OWNER2_TOKEN
                        );
                };

                Future<Result<Boolean>> future1 = executor.submit(task1);
                Future<Result<Boolean>> future2 = executor.submit(task2);

                startLatch.countDown();

                Result<Boolean> result1 = future1.get();
                Result<Boolean> result2 = future2.get();

                assertTrue(result1.isSuccess());
                assertTrue(result2.isSuccess());

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(updated.hasPendingOwnerInvite("a@test.com",OWNER1_EMAIL));
                assertTrue(updated.hasPendingOwnerInvite("b@test.com",OWNER2_EMAIL));

                executor.shutdown();
        }

        @Test
        void concurrentAssignOwner_sameTargetDifferentOwners_systemRemainsConsistent()throws Exception {

                ExecutorService executor =
                        Executors.newFixedThreadPool(2);

                CountDownLatch startLatch =
                        new CountDownLatch(1);

                Callable<Result<Boolean>> task1 = () -> {
                        startLatch.await();

                        return CompanyHierarchyService.assignOwnerToCompany(
                        COMPANY1_ID,
                        BYSTANDER_EMAIL,
                        VALID_OWNER1_TOKEN
                        );
                };

                Callable<Result<Boolean>> task2 = () -> {
                        startLatch.await();

                        return CompanyHierarchyService.assignOwnerToCompany(
                        COMPANY1_ID,
                        BYSTANDER_EMAIL,
                        VALID_OWNER2_TOKEN
                        );
                };

                Future<Result<Boolean>> future1 =
                        executor.submit(task1);

                Future<Result<Boolean>> future2 =
                        executor.submit(task2);

                startLatch.countDown();

                Result<Boolean> result1 = future1.get();
                Result<Boolean> result2 = future2.get();

                assertTrue(
                        result1.isSuccess() || result2.isSuccess()
                );

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(updated.hasPendingOwnerInvite(BYSTANDER_EMAIL,OWNER1_EMAIL) &&
                        updated.hasPendingOwnerInvite(BYSTANDER_EMAIL,OWNER2_EMAIL)
                );

                executor.shutdown();
        }

        @Test
        void concurrentAssignOwner_sameTargetSameOwner_systemRemainsConsistent()throws Exception {

                ExecutorService executor =
                        Executors.newFixedThreadPool(2);

                CountDownLatch startLatch =
                        new CountDownLatch(1);

                Callable<Result<Boolean>> task1 = () -> {
                        startLatch.await();

                        return CompanyHierarchyService.assignOwnerToCompany(
                        COMPANY1_ID,
                        BYSTANDER_EMAIL,
                        VALID_OWNER1_TOKEN
                        );
                };

                Callable<Result<Boolean>> task2 = () -> {
                        startLatch.await();

                        return CompanyHierarchyService.assignOwnerToCompany(
                        COMPANY1_ID,
                        BYSTANDER_EMAIL,
                        VALID_OWNER1_TOKEN
                        );
                };

                Future<Result<Boolean>> future1 =
                        executor.submit(task1);

                Future<Result<Boolean>> future2 =
                        executor.submit(task2);

                startLatch.countDown();

                Result<Boolean> result1 = future1.get();
                Result<Boolean> result2 = future2.get();

                assertTrue(
                        result1.isSuccess() || result2.isSuccess()
                );

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(updated.hasPendingOwnerInvite(BYSTANDER_EMAIL,OWNER1_EMAIL));

                executor.shutdown();
        }

        @Test
        void assignManagerToCompany_success() {
                assertFalse(company1.hasPendingInvite(BYSTANDER_EMAIL));

                Result<Boolean> result =
                        CompanyHierarchyService.assignManagerToCompany(
                        COMPANY1_ID,
                        BYSTANDER_EMAIL,
                        ALL_MANAGER_PERMISSIONS,
                        VALID_OWNER1_TOKEN
                        );

                assertTrue(result.isSuccess());
                assertTrue(result.getValue());

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(
                        updated.hasPendingManagerInvite(
                        BYSTANDER_EMAIL,
                        OWNER1_EMAIL,
                        ALL_MANAGER_PERMISSIONS
                        )
                );
        }

        @Test
        void assignManagerToCompany_invalidToken_fails() {

                Result<Boolean> result =
                        CompanyHierarchyService.assignManagerToCompany(
                        COMPANY1_ID,
                        BYSTANDER_EMAIL,
                        EnumSet.of(ManagerPermissions.CUSTOMER_SUPPORT),
                        INVALID_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals("Invalid Token", result.getError());

                verifyUserDidntGetInvite(BYSTANDER_EMAIL);
        }

        @Test
        void assignManagerToCompany_staleUser_fails() {

                Result<Boolean> result =
                        CompanyHierarchyService.assignManagerToCompany(
                        COMPANY1_ID,
                        BYSTANDER_EMAIL,
                        EnumSet.of(ManagerPermissions.CUSTOMER_SUPPORT),
                        STALE_USER_TOKEN
                        );

                assertFalse(result.isSuccess());

                assertEquals(
                        "User with ID " + BAD_USER_EMAIL + " not found.",
                        result.getError());

                verifyUserDidntGetInvite(BYSTANDER_EMAIL);
        }

        @Test
        void assignManagerToCompany_targetUserNotFound() {

                Result<Boolean> result =
                        CompanyHierarchyService.assignManagerToCompany(
                        COMPANY1_ID,
                        BAD_USER_EMAIL,
                        EnumSet.of(ManagerPermissions.CUSTOMER_SUPPORT),
                        VALID_OWNER1_TOKEN
                        );

                assertFalse(result.isSuccess());

                assertEquals(
                        "User with ID " + BAD_USER_EMAIL + " not found.",
                        result.getError()
                );

                verifyUserDidntGetInvite(BAD_USER_EMAIL);
        }

        @Test
        void assignManagerToCompany_companyNotFound() {

                Result<Boolean> result =
                        CompanyHierarchyService.assignManagerToCompany(
                        BAD_COMPANY_ID,
                        BYSTANDER_EMAIL,
                        EnumSet.of(ManagerPermissions.CUSTOMER_SUPPORT),
                        VALID_OWNER1_TOKEN
                        );

                assertFalse(result.isSuccess());

                assertEquals(
                        "Production company with ID "
                        + BAD_COMPANY_ID
                        + " is not found.",
                        result.getError()
                );
        }

        @Test
        void assignManagerToCompany_bystanderCannotAssign() {

                Result<Boolean> result =
                        CompanyHierarchyService.assignManagerToCompany(
                        COMPANY1_ID,
                        BYSTANDER_EMAIL,
                        EnumSet.of(ManagerPermissions.CUSTOMER_SUPPORT),
                        VALID_BYSTANDER_TOKEN
                        );

                assertFalse(result.isSuccess());

                assertTrue(
                        result.getError().contains(
                        "caller User is not owner in Assign Manager"
                        )
                );

                verifyUserDidntGetInvite(BYSTANDER_EMAIL);
        }

        @Test
        void assignManagerToCompany_managerCannotAssign() {

                Result<Boolean> result =
                        CompanyHierarchyService.assignManagerToCompany(
                        COMPANY1_ID,
                        BYSTANDER_EMAIL,
                        EnumSet.of(ManagerPermissions.CUSTOMER_SUPPORT),
                        VALID_MANAGER1_TOKEN
                        );

                assertFalse(result.isSuccess());

                assertTrue(
                        result.getError().contains(
                        "caller User is not owner in Assign Manager"
                        )
                );

                verifyUserDidntGetInvite(BYSTANDER_EMAIL);
        }

        @Test
        void assignManagerToCompany_existingManager_fails() {

                Result<Boolean> result =
                        CompanyHierarchyService.assignManagerToCompany(
                        COMPANY1_ID,
                        MANAGER1_EMAIL,
                        EnumSet.of(ManagerPermissions.CUSTOMER_SUPPORT),
                        VALID_OWNER1_TOKEN
                        );

                assertFalse(result.isSuccess());

                assertEquals(
                        "Target " + MANAGER1_EMAIL +
                        " is already a manager of the company.",
                        result.getError()
                );

                verifyUserDidntGetInvite(MANAGER1_EMAIL);
        }

        @Test
        void assignManagerToCompany_duplicatePendingInvite_allowed() {

                assertTrue(
                        company1.hasPendingInvite(INVITED_MANAGER_EMAIL)
                );

                Set<ManagerPermissions> permissions =
                        EnumSet.allOf(ManagerPermissions.class);

                Result<Boolean> result =
                        CompanyHierarchyService.assignManagerToCompany(
                        COMPANY1_ID,
                        INVITED_MANAGER_EMAIL,
                        permissions,
                        VALID_OWNER1_TOKEN
                        );

                assertTrue(result.isSuccess());

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(
                        updated.hasPendingManagerInvite(
                        INVITED_MANAGER_EMAIL,
                        OWNER1_EMAIL,
                        permissions
                        )
                );
        }
        
        @Test
        void assignManagerToCompany_InviteSomeoneWithOwnerInvite_allowed() {

                assertTrue(
                        company1.hasPendingInvite(INVITED_MANAGER_EMAIL)
                );

                Result<Boolean> result =
                        CompanyHierarchyService.assignManagerToCompany(
                        COMPANY1_ID,
                        INVITED_OWNER_EMAIL,
                        ALL_MANAGER_PERMISSIONS,
                        VALID_OWNER1_TOKEN
                        );

                assertTrue(result.isSuccess());

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(
                        updated.hasPendingManagerInvite(
                        INVITED_OWNER_EMAIL,
                        OWNER1_EMAIL,
                        ALL_MANAGER_PERMISSIONS
                        )
                );
                assertTrue(updated.hasPendingOwnerInvite(INVITED_OWNER_EMAIL, OWNER2_EMAIL));
        }

        @Test
        void assignManagerToCompany_emptyPermissions_fails() {
                Result<Boolean> result =
                        CompanyHierarchyService.assignManagerToCompany(
                        COMPANY1_ID,
                        BYSTANDER_EMAIL,
                        Collections.emptySet(),
                        VALID_OWNER1_TOKEN
                        );

                assertFalse(result.isSuccess());

                assertEquals(
                        "Manager must have at least one permission.",
                        result.getError()
                );
                verifyUserDidntGetInvite(BYSTANDER_EMAIL);
        }

        @Test
        void assignManagerToCompany_nullPermissions_fails() {

                Result<Boolean> result =
                        CompanyHierarchyService.assignManagerToCompany(
                        COMPANY1_ID,
                        BYSTANDER_EMAIL,
                        null,
                        VALID_OWNER1_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals(
                        "Manager must have at least one permission.",
                        result.getError()
                );

                verifyUserDidntGetInvite(BYSTANDER_EMAIL);
        }

        @Test
        void assignManagerToCompany_unexpectedException() {

                IProductionCompanyRepository repo =
                        mock(IProductionCompanyRepository.class);

                when(repo.findByID(anyString()))
                        .thenThrow(new RuntimeException("DB exploded"));

                CompanyHierarchyService service =
                        new CompanyHierarchyService(
                        mockAuthService,
                        repo,
                        UserRepository
                        );

                Result<Boolean> result =
                        service.assignManagerToCompany(
                        COMPANY1_ID,
                        BYSTANDER_EMAIL,
                        EnumSet.of(ManagerPermissions.CUSTOMER_SUPPORT),
                        VALID_OWNER1_TOKEN
                        );

                assertFalse(result.isSuccess());

                assertTrue(
                        result.getError().contains("unexpected")
                );

                verify(repo, never()).save(any());
        }

        @Test
        void concurrentAssignManager_differentTargets_bothSucceed()
        throws Exception {

                User userA = new User("managerA@test.com", "password");
                User userB = new User("managerB@test.com", "password");

                UserRepository.save(userA);
                UserRepository.save(userB);

                ExecutorService executor =
                        Executors.newFixedThreadPool(2);

                CountDownLatch startLatch =
                        new CountDownLatch(1);

                Callable<Result<Boolean>> task1 = () -> {

                        startLatch.await();

                        return CompanyHierarchyService.assignManagerToCompany(
                                COMPANY1_ID,
                                "managerA@test.com",
                                EnumSet.of(
                                        ManagerPermissions.CUSTOMER_SUPPORT
                                ),
                                VALID_OWNER1_TOKEN
                        );
                };

                Callable<Result<Boolean>> task2 = () -> {

                        startLatch.await();

                        return CompanyHierarchyService.assignManagerToCompany(
                                COMPANY1_ID,
                                "managerB@test.com",
                                EnumSet.of(
                                        ManagerPermissions.PURCHASE_POLICY
                                ),
                                VALID_OWNER2_TOKEN
                        );
                };

                Future<Result<Boolean>> future1 =
                        executor.submit(task1);

                Future<Result<Boolean>> future2 =
                        executor.submit(task2);

                startLatch.countDown();

                Result<Boolean> result1 = future1.get();
                Result<Boolean> result2 = future2.get();

                assertTrue(result1.isSuccess());
                assertTrue(result2.isSuccess());

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(
                        updated.hasPendingManagerInvite(
                        "managerA@test.com",
                        OWNER1_EMAIL,
                        EnumSet.of(
                                ManagerPermissions.CUSTOMER_SUPPORT
                        )
                        )
                );

                assertTrue(
                        updated.hasPendingManagerInvite(
                        "managerB@test.com",
                        OWNER2_EMAIL,
                        EnumSet.of(
                                ManagerPermissions.PURCHASE_POLICY
                        )
                        )
                );

                executor.shutdown();
        }

        @Test
        void concurrentAssignManager_sameTargetDifferentOwners_consistent()
        throws Exception {

                ExecutorService executor =
                        Executors.newFixedThreadPool(2);

                CountDownLatch startLatch =
                        new CountDownLatch(1);

                Callable<Result<Boolean>> task1 = () -> {

                        startLatch.await();

                        return CompanyHierarchyService.assignManagerToCompany(
                                COMPANY1_ID,
                                BYSTANDER_EMAIL,
                                EnumSet.of(
                                        ManagerPermissions.CUSTOMER_SUPPORT
                                ),
                                VALID_OWNER1_TOKEN
                        );
                };

                Callable<Result<Boolean>> task2 = () -> {

                        startLatch.await();

                        return CompanyHierarchyService.assignManagerToCompany(
                                COMPANY1_ID,
                                BYSTANDER_EMAIL,
                                EnumSet.of(
                                        ManagerPermissions.PURCHASE_POLICY
                                ),
                                VALID_OWNER2_TOKEN
                        );
                };

                Future<Result<Boolean>> future1 =
                        executor.submit(task1);

                Future<Result<Boolean>> future2 =
                        executor.submit(task2);

                startLatch.countDown();

                Result<Boolean> result1 = future1.get();
                Result<Boolean> result2 = future2.get();

                assertTrue(
                        result1.isSuccess() || result2.isSuccess()
                );

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(
                        updated.hasPendingInvite(BYSTANDER_EMAIL)
                );

                executor.shutdown();
        }

        @Test
        void concurrentAssignManager_sameTargetSameOwner_consistent()
        throws Exception {

                ExecutorService executor =
                        Executors.newFixedThreadPool(2);

                CountDownLatch startLatch =
                        new CountDownLatch(1);

                Callable<Result<Boolean>> task1 = () -> {

                        startLatch.await();

                        return CompanyHierarchyService.assignManagerToCompany(
                                COMPANY1_ID,
                                BYSTANDER_EMAIL,
                                EnumSet.of(
                                        ManagerPermissions.CUSTOMER_SUPPORT
                                ),
                                VALID_OWNER1_TOKEN
                        );
                };

                Callable<Result<Boolean>> task2 = () -> {

                        startLatch.await();

                        return CompanyHierarchyService.assignManagerToCompany(
                                COMPANY1_ID,
                                BYSTANDER_EMAIL,
                                EnumSet.of(
                                        ManagerPermissions.CUSTOMER_SUPPORT
                                ),
                                VALID_OWNER1_TOKEN
                        );
                };

                Future<Result<Boolean>> future1 =
                        executor.submit(task1);

                Future<Result<Boolean>> future2 =
                        executor.submit(task2);

                startLatch.countDown();

                Result<Boolean> result1 = future1.get();
                Result<Boolean> result2 = future2.get();

                assertTrue(
                        result1.isSuccess() || result2.isSuccess()
                );

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(
                        updated.hasPendingManagerInvite(
                        BYSTANDER_EMAIL,
                        OWNER1_EMAIL,
                        EnumSet.of(
                                ManagerPermissions.CUSTOMER_SUPPORT
                        )
                        )
                );

                executor.shutdown();
        }

        @Test
        void concurrentAssignManager_sameTargetDifferentPermissions_consistent()
        throws Exception {

                ExecutorService executor =
                        Executors.newFixedThreadPool(2);

                CountDownLatch startLatch =
                        new CountDownLatch(1);

                Set<ManagerPermissions> permissions1 =
                        EnumSet.of(
                        ManagerPermissions.CUSTOMER_SUPPORT
                        );

                Set<ManagerPermissions> permissions2 =
                        EnumSet.of(
                        ManagerPermissions.PURCHASE_POLICY
                        );

                Callable<Result<Boolean>> task1 = () -> {

                        startLatch.await();

                        return CompanyHierarchyService.assignManagerToCompany(
                                COMPANY1_ID,
                                BYSTANDER_EMAIL,
                                permissions1,
                                VALID_OWNER1_TOKEN
                        );
                };

                Callable<Result<Boolean>> task2 = () -> {

                        startLatch.await();

                        return CompanyHierarchyService.assignManagerToCompany(
                                COMPANY1_ID,
                                BYSTANDER_EMAIL,
                                permissions2,
                                VALID_OWNER1_TOKEN
                        );
                };

                Future<Result<Boolean>> future1 =
                        executor.submit(task1);

                Future<Result<Boolean>> future2 =
                        executor.submit(task2);

                startLatch.countDown();

                Result<Boolean> result1 = future1.get();
                Result<Boolean> result2 = future2.get();

                assertTrue(
                        result1.isSuccess() || result2.isSuccess()
                );

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(
                        updated.hasPendingInvite(BYSTANDER_EMAIL)
                );

                executor.shutdown();
        }

        @Test
        void forfeitOwnership_success() {
                Result<Boolean> result =
                        CompanyHierarchyService.forfeitOwnership(
                        COMPANY1_ID,
                        VALID_OWNER2_TOKEN
                        );

                assertTrue(result.isSuccess());
                assertTrue(result.getValue());

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertFalse(updated.isOwner(OWNER2_EMAIL));
                assertTrue(updated.areDirectSubordinates(OWNER1_EMAIL, OWNER2_DEFAULT_CHILDREN));
                assertTrue(updated.areDirectSubordinates(OWNER2_EMAIL, Collections.emptySet()));
                OWNER1_DEFAULT_CHILDREN.remove(OWNER2_EMAIL);
                assertTrue(updated.areDirectSubordinates(OWNER1_EMAIL, OWNER1_DEFAULT_CHILDREN));
        }

        @Test
        void forfeitOwnership_invalidToken_fails() {

                Result<Boolean> result =
                        CompanyHierarchyService.forfeitOwnership(
                        COMPANY1_ID,
                        INVALID_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals("Invalid Token", result.getError());

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(updated.isOwner(OWNER2_EMAIL));
                children_didnt_change();
                
        }

        @Test
        void forfeitOwnership_staleUser_fails() {

                Result<Boolean> result =
                        CompanyHierarchyService.forfeitOwnership(
                        COMPANY1_ID,
                        STALE_USER_TOKEN
                        );

                assertFalse(result.isSuccess());

                assertEquals(
                        "User with ID " + BAD_USER_EMAIL + " not found.",
                        result.getError()
                );

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(updated.isOwner(OWNER2_EMAIL));
                children_didnt_change();
        }

        @Test
        void forfeitOwnership_companyNotFound() {

                Result<Boolean> result =
                        CompanyHierarchyService.forfeitOwnership(
                        BAD_COMPANY_ID,
                        VALID_OWNER2_TOKEN
                        );

                assertFalse(result.isSuccess());

                assertEquals(
                        "Production company with ID "
                        + BAD_COMPANY_ID
                        + " is not found.",
                        result.getError()
                );
                children_didnt_change();
        }

        @Test
        void forfeitOwnership_bystanderCannotForfeit() {

                Result<Boolean> result =
                        CompanyHierarchyService.forfeitOwnership(
                        COMPANY1_ID,
                        VALID_BYSTANDER_TOKEN
                        );

                assertFalse(result.isSuccess());

                assertEquals(
                        "User " + BYSTANDER_EMAIL + " is not owner in forfeit Ownership",
                        result.getError()
                );

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertFalse(updated.isOwner(BYSTANDER_EMAIL));
                children_didnt_change();
        }

        @Test
        void forfeitOwnership_managerCannotForfeit() {

                Result<Boolean> result =
                        CompanyHierarchyService.forfeitOwnership(
                        COMPANY1_ID,
                        VALID_MANAGER1_TOKEN
                        );

                assertFalse(result.isSuccess());

                assertEquals(
                        "User " + MANAGER1_EMAIL + " is not owner in forfeit Ownership",
                        result.getError()
                );

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertFalse(updated.isOwner(MANAGER1_EMAIL));
                children_didnt_change();
        }

        @Test
        void forfeitOwnership_founderCannotForfeit() {

                Result<Boolean> result =
                        CompanyHierarchyService.forfeitOwnership(
                        COMPANY1_ID,
                        VALID_FOUNDER_TOKEN
                        );

                assertFalse(result.isSuccess());

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(updated.isFounder(FOUNDER_EMAIL));
                children_didnt_change();
        }

        @Test
        void forfeitOwnership_unexpectedException() {
                IProductionCompanyRepository repo =
                        mock(IProductionCompanyRepository.class);

                when(repo.findByID(anyString()))
                        .thenThrow(new RuntimeException("DB exploded"));

                CompanyHierarchyService service =
                        new CompanyHierarchyService(
                        mockAuthService,
                        repo,
                        UserRepository
                        );

                Result<Boolean> result =
                        service.forfeitOwnership(
                        COMPANY1_ID,
                        VALID_OWNER2_TOKEN
                        );

                assertFalse(result.isSuccess());

                assertTrue(
                        result.getError().contains("unexpected")
                );

                verify(repo, never()).save(any());
        }

        @Test
        void concurrentForfeitOwnership_sameOwner_onlyOneSucceeds()
        throws Exception {

                ExecutorService executor =
                        Executors.newFixedThreadPool(2);

                CountDownLatch startLatch =
                        new CountDownLatch(1);

                Callable<Result<Boolean>> task = () -> {

                        startLatch.await();

                        return CompanyHierarchyService.forfeitOwnership(
                                COMPANY1_ID,
                                VALID_OWNER2_TOKEN
                        );
                };

                Future<Result<Boolean>> future1 =
                        executor.submit(task);

                Future<Result<Boolean>> future2 =
                        executor.submit(task);

                startLatch.countDown();

                Result<Boolean> result1 = future1.get();
                Result<Boolean> result2 = future2.get();

                assertTrue(
                        result1.isSuccess() || result2.isSuccess()
                );

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertFalse(updated.isOwner(OWNER2_EMAIL));

                executor.shutdown();
        }

        @Test
        void concurrentForfeitOwnership_differentOwners_bothSucceed()
        throws Exception {

                ExecutorService executor =
                        Executors.newFixedThreadPool(2);

                CountDownLatch startLatch =
                        new CountDownLatch(1);

                Callable<Result<Boolean>> task1 = () -> {

                        startLatch.await();

                        return CompanyHierarchyService.forfeitOwnership(
                                COMPANY1_ID,
                                VALID_OWNER1_TOKEN
                        );
                };

                Callable<Result<Boolean>> task2 = () -> {

                        startLatch.await();

                        return CompanyHierarchyService.forfeitOwnership(
                                COMPANY1_ID,
                                VALID_OWNER2_TOKEN
                        );
                };

                Future<Result<Boolean>> future1 =
                        executor.submit(task1);

                Future<Result<Boolean>> future2 =
                        executor.submit(task2);

                startLatch.countDown();

                Result<Boolean> result1 = future1.get();
                Result<Boolean> result2 = future2.get();

                assertTrue(result1.isSuccess());
                assertTrue(result2.isSuccess());

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertFalse(updated.isOwner(OWNER1_EMAIL));
                assertFalse(updated.isOwner(OWNER2_EMAIL));

                executor.shutdown();
        }

        @Test
        void acceptInviteToCompany_success_ownerInvite() {
                assertTrue(
                        company1.hasPendingOwnerInvite(
                        INVITED_OWNER_EMAIL,
                        OWNER2_EMAIL
                        )
                );

                Result<Boolean> result =
                        CompanyHierarchyService.acceptInviteToCompany(
                        COMPANY1_ID,
                        OWNER2_EMAIL,
                        VALID_INVITED_OWNER_TOKEN
                        );

                assertTrue(result.isSuccess());

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(updated.isOwner(INVITED_OWNER_EMAIL));
                assertFalse(
                        updated.hasPendingInvite(
                        INVITED_OWNER_EMAIL,
                        OWNER1_EMAIL
                        )
                );
        }

        @Test
        void acceptInviteToCompany_success_managerInvite() {
                assertTrue(
                        company1.hasPendingManagerInvite(
                        INVITED_MANAGER_EMAIL,
                        OWNER1_EMAIL,
                        ALL_MANAGER_PERMISSIONS
                        )
                );

                Result<Boolean> result =
                        CompanyHierarchyService.acceptInviteToCompany(
                        COMPANY1_ID,
                        OWNER1_EMAIL,
                        VALID_INVITED_OWNER_TOKEN
                        );

                assertTrue(result.isSuccess());
                assertTrue(result.getValue());

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(updated.isManager(INVITED_OWNER_EMAIL));
                assertFalse(
                        updated.hasPendingManagerInvite(
                        INVITED_OWNER_EMAIL,
                        OWNER1_EMAIL,
                        ALL_MANAGER_PERMISSIONS
                        )
                );
                assertTrue(
                        updated.hasPendingOwnerInvite(
                        INVITED_OWNER_EMAIL,
                        OWNER2_EMAIL
                        )
                );
        }

        @Test
        void acceptInviteToCompany_invalidToken_fails() {
                Result<Boolean> result =
                        CompanyHierarchyService.acceptInviteToCompany(
                        COMPANY1_ID,
                        OWNER1_EMAIL,
                        INVALID_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals("Invalid Token", result.getError());
        }

        @Test
        void acceptInviteToCompany_staleUser_fails() {
                Result<Boolean> result =
                        CompanyHierarchyService.acceptInviteToCompany(
                        COMPANY1_ID,
                        OWNER1_EMAIL,
                        STALE_USER_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals(
                        "User with ID " + BAD_USER_EMAIL + " not found.",
                        result.getError()
                );
        }

        @Test
        void acceptInviteToCompany_assignerNotFound() {
                Result<Boolean> result =
                        CompanyHierarchyService.acceptInviteToCompany(
                        COMPANY1_ID,
                        BAD_USER_EMAIL,
                        VALID_INVITED_OWNER_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals(
                        "User with ID " + BAD_USER_EMAIL + " not found.",
                        result.getError()
                );
        }

        @Test
        void acceptInviteToCompany_companyNotFound() {
                Result<Boolean> result =
                        CompanyHierarchyService.acceptInviteToCompany(
                        BAD_COMPANY_ID,
                        OWNER1_EMAIL,
                        VALID_INVITED_OWNER_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals(
                        "Production company with ID " +
                        BAD_COMPANY_ID +
                        " is not found.",
                        result.getError()
                );
        }

        @Test
        void acceptInviteToCompany_inviteNotFound() {
                assertFalse(
                        company1.hasPendingInvite(BYSTANDER_EMAIL)
                );

                Result<Boolean> result =
                        CompanyHierarchyService.acceptInviteToCompany(
                        COMPANY1_ID,
                        OWNER1_EMAIL,
                        VALID_BYSTANDER_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals("Invite not found.", result.getError());
        }

        @Test
        void acceptInviteToCompany_existingManagerAcceptsOwnerInvite_roleUpgraded() {
                Result<Boolean> result =
                        CompanyHierarchyService.acceptInviteToCompany(
                        COMPANY1_ID,
                        OWNER1_EMAIL,
                        VALID_MANAGER2_TOKEN
                        );

                assertTrue(result.isSuccess());

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(updated.isOwner(MANAGER2_EMAIL));
        }

        @Test
        void acceptInviteToCompany_unexpectedException() {

                IProductionCompanyRepository repo =
                        mock(IProductionCompanyRepository.class);

                when(repo.findByID(anyString()))
                        .thenThrow(new RuntimeException("DB exploded"));

                CompanyHierarchyService service =
                        new CompanyHierarchyService(
                        mockAuthService,
                        repo,
                        UserRepository
                        );

                Result<Boolean> result =
                        service.acceptInviteToCompany(
                        COMPANY1_ID,
                        OWNER1_EMAIL,
                        VALID_INVITED_OWNER_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertTrue(
                        result.getError().contains("unexpected")
                );

                verify(repo, never()).save(any());
        }

        @Test
        void concurrentAcceptInvite_sameInvite_systemRemainsConsistent()
        throws Exception {

                ExecutorService executor =
                        Executors.newFixedThreadPool(2);

                CountDownLatch startLatch =
                        new CountDownLatch(1);

                Callable<Result<Boolean>> task1 = () -> {
                        startLatch.await();

                        return CompanyHierarchyService.acceptInviteToCompany(
                                COMPANY1_ID,
                                OWNER2_EMAIL,
                                VALID_INVITED_OWNER_TOKEN
                        );
                };

                Callable<Result<Boolean>> task2 = () -> {
                        startLatch.await();

                        return CompanyHierarchyService.acceptInviteToCompany(
                                COMPANY1_ID,
                                OWNER2_EMAIL,
                                VALID_INVITED_OWNER_TOKEN
                        );
                };

                Future<Result<Boolean>> future1 =
                        executor.submit(task1);

                Future<Result<Boolean>> future2 =
                        executor.submit(task2);

                startLatch.countDown();

                Result<Boolean> result1 = future1.get();
                Result<Boolean> result2 = future2.get();

                assertTrue(
                        result1.isSuccess() ||
                        result2.isSuccess()
                );

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(updated.isOwner(INVITED_OWNER_EMAIL));

                assertFalse(
                        updated.hasPendingOwnerInvite(
                        INVITED_OWNER_EMAIL,
                        OWNER1_EMAIL
                        )
                );

                executor.shutdown();
        }

        @Test
        void rejectInviteToCompany_success_ownerInviteRejected() {

                assertTrue(
                        company1.hasPendingOwnerInvite(
                        INVITED_OWNER_EMAIL,
                        OWNER2_EMAIL
                        )
                );

                Result<Boolean> result =
                        CompanyHierarchyService.rejectInviteToCompany(
                        COMPANY1_ID,
                        OWNER2_EMAIL,
                        VALID_INVITED_OWNER_TOKEN
                        );

                assertTrue(result.isSuccess());
                assertTrue(result.getValue());

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertFalse(
                        updated.hasPendingOwnerInvite(
                        INVITED_OWNER_EMAIL,
                        OWNER2_EMAIL
                        )
                );
                assertTrue(
                        updated.hasPendingManagerInvite(
                        INVITED_OWNER_EMAIL,
                        OWNER1_EMAIL,
                        ALL_MANAGER_PERMISSIONS
                        )
                );

                assertFalse(updated.isOwner(INVITED_OWNER_EMAIL));
        }

        @Test
        void rejectInviteToCompany_invalidToken_fails() {

                Result<Boolean> result =
                        CompanyHierarchyService.rejectInviteToCompany(
                        COMPANY1_ID,
                        OWNER2_EMAIL,
                        INVALID_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals("Invalid Token", result.getError());
                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );
                assertTrue(updated.hasPendingOwnerInvite(
                        INVITED_OWNER_EMAIL,
                        OWNER2_EMAIL
                        )
                );
        }

        @Test
        void rejectInviteToCompany_staleUser_fails() {
                Result<Boolean> result =
                        CompanyHierarchyService.rejectInviteToCompany(
                        COMPANY1_ID,
                        OWNER2_EMAIL,
                        STALE_USER_TOKEN
                        );

                assertFalse(result.isSuccess());

                assertEquals(
                        "User with ID " + BAD_USER_EMAIL + " not found.",
                        result.getError()
                );
                
        }

        @Test
        void rejectInviteToCompany_companyNotFound() {
                Result<Boolean> result =
                        CompanyHierarchyService.rejectInviteToCompany(
                        BAD_COMPANY_ID,
                        OWNER1_EMAIL,
                        VALID_INVITED_OWNER_TOKEN
                        );

                assertFalse(result.isSuccess());

                assertEquals(
                        "Production company with ID " +
                        BAD_COMPANY_ID +
                        " is not found.",
                        result.getError()
                );
        }

        @Test
        void rejectInviteToCompany_inviteNotFound() {
                assertFalse(
                        company1.hasPendingInvite(BYSTANDER_EMAIL)
                );

                Result<Boolean> result =
                        CompanyHierarchyService.rejectInviteToCompany(
                        COMPANY1_ID,
                        OWNER1_EMAIL,
                        VALID_BYSTANDER_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals("Invite not found.", result.getError());
        }

        @Test
        void concurrentRejectInvite_sameInvite_onlyOneSucceeds()
        throws Exception {

                ExecutorService executor =
                        Executors.newFixedThreadPool(2);

                CountDownLatch startLatch =
                        new CountDownLatch(1);

                Callable<Result<Boolean>> task1 = () -> {
                        startLatch.await();

                        return CompanyHierarchyService.rejectInviteToCompany(
                                COMPANY1_ID,
                                OWNER2_EMAIL,
                                VALID_INVITED_OWNER_TOKEN
                        );
                };

                Callable<Result<Boolean>> task2 = () -> {
                        startLatch.await();

                        return CompanyHierarchyService.rejectInviteToCompany(
                                COMPANY1_ID,
                                OWNER2_EMAIL,
                                VALID_INVITED_OWNER_TOKEN
                        );
                };

                Future<Result<Boolean>> future1 =
                        executor.submit(task1);

                Future<Result<Boolean>> future2 =
                        executor.submit(task2);

                startLatch.countDown();

                Result<Boolean> result1 = future1.get();
                Result<Boolean> result2 = future2.get();

                assertTrue(
                        result1.isSuccess() ^
                        result2.isSuccess()
                );

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertFalse(
                        updated.hasPendingOwnerInvite(
                        INVITED_OWNER_EMAIL,
                        OWNER2_EMAIL
                        )
                );

                executor.shutdown();
        }

        @Test
        void concurrentRejectInvite_differentInvites_bothSucceed()
        throws Exception {

                ExecutorService executor =
                        Executors.newFixedThreadPool(2);

                CountDownLatch startLatch =
                        new CountDownLatch(1);

                Callable<Result<Boolean>> task1 = () -> {
                        startLatch.await();

                        return CompanyHierarchyService.rejectInviteToCompany(
                                COMPANY1_ID,
                                OWNER2_EMAIL,
                                VALID_INVITED_OWNER_TOKEN
                        );
                };

                Callable<Result<Boolean>> task2 = () -> {
                        startLatch.await();

                        return CompanyHierarchyService.rejectInviteToCompany(
                                COMPANY1_ID,
                                OWNER1_EMAIL,
                                VALID_INVITED_MANAGER_TOKEN
                        );
                };

                Future<Result<Boolean>> future1 =
                        executor.submit(task1);

                Future<Result<Boolean>> future2 =
                        executor.submit(task2);

                startLatch.countDown();

                Result<Boolean> result1 = future1.get();
                Result<Boolean> result2 = future2.get();

                assertTrue(result1.isSuccess());
                assertTrue(result2.isSuccess());

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertFalse(
                        updated.hasPendingOwnerInvite(
                        INVITED_OWNER_EMAIL,
                        OWNER2_EMAIL
                        )
                );

                assertFalse(
                        updated.hasPendingManagerInvite(
                        INVITED_MANAGER_EMAIL,
                        OWNER1_EMAIL,
                        ALL_MANAGER_PERMISSIONS
                        )
                );

                executor.shutdown();
        }

        @Test
        void removeManager_success() {
                //also tests transitive removal
                Result<Boolean> result =
                        CompanyHierarchyService.removeOwnerManager(
                        MANAGER2_EMAIL,
                        COMPANY1_ID,
                        VALID_OWNER1_TOKEN
                        );

                assertTrue(result.isSuccess());

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertFalse(updated.isManager(MANAGER2_EMAIL));
                assertFalse(updated.isDirectSubordinate(OWNER2_EMAIL, MANAGER2_EMAIL));
        }

        @Test
        void removeOwner_success()
        {
                Result<Boolean> result =
                        CompanyHierarchyService.removeOwnerManager(
                        OWNER2_EMAIL,
                        COMPANY1_ID,
                        VALID_OWNER1_TOKEN
                        );

                assertTrue(result.isSuccess());

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertFalse(updated.isOwner(OWNER2_EMAIL));
                OWNER1_DEFAULT_CHILDREN.remove(OWNER2_EMAIL);
                assertTrue(updated.areDirectSubordinates(OWNER1_EMAIL, OWNER1_DEFAULT_CHILDREN));
                assertTrue(updated.areDirectSubordinates(OWNER1_EMAIL, OWNER2_DEFAULT_CHILDREN));
                assertFalse(updated.isDirectSubordinate(OWNER1_EMAIL, OWNER2_EMAIL));
        }

        @Test
        void removeOwnerManager_invalidToken_fails()
        {
                Result<Boolean> result =
                        CompanyHierarchyService.removeOwnerManager(
                        MANAGER1_EMAIL,
                        COMPANY1_ID,
                        INVALID_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals("Invalid Token", result.getError());

                children_didnt_change();
        }

        @Test
        void removeOwnerManager_staleUser_fails()
        {
                Result<Boolean> result =
                        CompanyHierarchyService.removeOwnerManager(
                        MANAGER1_EMAIL,
                        COMPANY1_ID,
                        STALE_USER_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals(
                        "User with ID " + BAD_USER_EMAIL + " not found.",
                        result.getError()
                );

                children_didnt_change();
        }

        @Test
        void removeOwnerManager_companyNotFound()
        {
                Result<Boolean> result =
                        CompanyHierarchyService.removeOwnerManager(
                        MANAGER1_EMAIL,
                        BAD_COMPANY_ID,
                        VALID_OWNER1_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals(
                        "Production company with ID "
                        + BAD_COMPANY_ID
                        + " is not found.",
                        result.getError()
                );

                children_didnt_change();
        }

        @Test
        void removeOwnerManager_nonOwnerCannotRemove()
        {
                Result<Boolean> result =
                        CompanyHierarchyService.removeOwnerManager(
                        MANAGER1_EMAIL,
                        COMPANY1_ID,
                        VALID_MANAGER1_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals(
                        "Caller is not an owner.",
                        result.getError()
                );


                children_didnt_change();
        }

        @Test
        void removeOwnerManager_ownerCannotRemoveAManagerNotAssignedByHim()
        {
                Result<Boolean> result =
                        CompanyHierarchyService.removeOwnerManager(
                        MANAGER1_EMAIL,
                        COMPANY1_ID,
                        VALID_OWNER2_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals(
                        "Target was not assigned by this owner (directly or transitively).",
                        result.getError()
                );

                children_didnt_change();
        }

        @Test
        void removeOwnerManager_targetIsntFound_failue()
        {
                Result<Boolean> result =
                        CompanyHierarchyService.removeOwnerManager(
                        BAD_USER_EMAIL,
                        COMPANY1_ID,
                        VALID_OWNER1_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals(
                        "User with ID " + BAD_USER_EMAIL + " not found.",
                        result.getError()
                );

                children_didnt_change();
        }

        @Test
        void removeOwnerManager_targetNotOwnerOrManager_failue()
        {
                Result<Boolean> result =
                        CompanyHierarchyService.removeOwnerManager(
                        BYSTANDER_EMAIL,
                        COMPANY1_ID,
                        VALID_OWNER1_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals(
                        "Target is not a member.",
                        result.getError()
                );

                children_didnt_change();
        }

        @Test
        void removeOwner_removeItself_fail()
        {
                Result<Boolean> result =
                        CompanyHierarchyService.removeOwnerManager(
                        OWNER1_EMAIL,
                        COMPANY1_ID,
                        VALID_OWNER1_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals(
                        "Target was not assigned by this owner (directly or transitively).",
                        result.getError()
                );

                children_didnt_change();
        }

        @Test
        void concurrentRemoveManager_sameTarget_onlyOneSucceeds()
                throws Exception {

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        Callable<Result<Boolean>> task1 = () -> {
                startLatch.await();
                return CompanyHierarchyService.removeOwnerManager(
                        MANAGER2_EMAIL,
                        COMPANY1_ID,
                        VALID_OWNER1_TOKEN
                );
        };

        Callable<Result<Boolean>> task2 = () -> {
                startLatch.await();
                return CompanyHierarchyService.removeOwnerManager(
                        MANAGER2_EMAIL,
                        COMPANY1_ID,
                        VALID_OWNER1_TOKEN
                );
        };

        Future<Result<Boolean>> future1 = executor.submit(task1);
        Future<Result<Boolean>> future2 = executor.submit(task2);

        startLatch.countDown();

        Result<Boolean> result1 = future1.get();
        Result<Boolean> result2 = future2.get();

        ProductionCompany updated =
                ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                );

        // exactly one succeeds
        assertTrue(result1.isSuccess() ^ result2.isSuccess());

        // final state must be consistent
        assertFalse(updated.isManager(MANAGER2_EMAIL));
        assertFalse(updated.isDirectSubordinate(OWNER2_EMAIL, MANAGER2_EMAIL));

        executor.shutdown();
        }

        @Test
        void concurrentRemoveOwners_bothSucceed_subtreesReassignedToFounder()
                throws Exception {

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        Callable<Result<Boolean>> task1 = () -> {
                startLatch.await();
                return CompanyHierarchyService.removeOwnerManager(
                        OWNER1_EMAIL,
                        COMPANY1_ID,
                        VALID_FOUNDER_TOKEN
                );
        };

        Callable<Result<Boolean>> task2 = () -> {
                startLatch.await();
                return CompanyHierarchyService.removeOwnerManager(
                        OWNER2_EMAIL,
                        COMPANY1_ID,
                        VALID_FOUNDER_TOKEN
                );
        };

        Future<Result<Boolean>> future1 = executor.submit(task1);
        Future<Result<Boolean>> future2 = executor.submit(task2);

        startLatch.countDown();

        Result<Boolean> result1 = future1.get();
        Result<Boolean> result2 = future2.get();

        assertTrue(result1.isSuccess());
        assertTrue(result2.isSuccess());

        ProductionCompany updated =
                ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                );

        // both owners removed
        assertFalse(updated.isOwner(OWNER1_EMAIL));
        assertFalse(updated.isOwner(OWNER2_EMAIL));

        // founder should now own everything
        assertTrue(updated.isOwner(FOUNDER_EMAIL));

        Set<String> ALL_EXPECTED_CHILDREN_AFTER_MIGRATION = new HashSet<>();
        ALL_EXPECTED_CHILDREN_AFTER_MIGRATION.addAll(OWNER1_DEFAULT_CHILDREN);
        ALL_EXPECTED_CHILDREN_AFTER_MIGRATION.addAll(OWNER2_DEFAULT_CHILDREN);
        ALL_EXPECTED_CHILDREN_AFTER_MIGRATION.remove(OWNER2_EMAIL);
        // critical structural invariant:
        // all children must now belong to founder
        assertTrue(updated.areDirectSubordinates(
                FOUNDER_EMAIL,
                ALL_EXPECTED_CHILDREN_AFTER_MIGRATION
        ));

        // no dangling links
        assertFalse(updated.isDirectSubordinate(OWNER1_EMAIL, MANAGER1_EMAIL));
        assertFalse(updated.isDirectSubordinate(OWNER2_EMAIL, MANAGER2_EMAIL));

        executor.shutdown();
        }

        //changeManagerPermission tests
        @Test
        void changeManagerPermissions_success() {
                Result<Boolean> result =
                        CompanyHierarchyService.changeManagerPermission(
                        MANAGER1_EMAIL,
                        COMPANY1_ID,
                        NEW_MANAGER_PERMISSIONS,
                        VALID_OWNER1_TOKEN
                        );

                assertTrue(result.isSuccess());

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(
                        updated.hasManagerWithPermissions(
                        MANAGER1_EMAIL,
                        NEW_MANAGER_PERMISSIONS
                        )
                );
        }

        @Test
        void changeManagerPermissions_invalidToken_fails() {
                Result<Boolean> result =
                        CompanyHierarchyService.changeManagerPermission(
                        MANAGER1_EMAIL,
                        COMPANY1_ID,
                        NEW_MANAGER_PERMISSIONS,
                        INVALID_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals("Invalid Token", result.getError());

                //permissions didn't change
                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(
                        updated.hasManagerWithPermissions(
                        MANAGER1_EMAIL,
                        ALL_MANAGER_PERMISSIONS
                        )
                );
        }

        @Test
        void changeManagerPermissions_staleUser_fails() {
                Result<Boolean> result =
                        CompanyHierarchyService.changeManagerPermission(
                        MANAGER1_EMAIL,
                        COMPANY1_ID,
                        NEW_MANAGER_PERMISSIONS,
                        STALE_USER_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals(
                        "User with ID " + BAD_USER_EMAIL + " not found.",
                        result.getError()
                );

                //permissions didn't change
                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(
                        updated.hasManagerWithPermissions(
                        MANAGER1_EMAIL,
                        ALL_MANAGER_PERMISSIONS
                        )
                );
        }

         @Test
         void changeManagerPermissions_companyNotFound() {
                 Result<Boolean> result =
                         CompanyHierarchyService.changeManagerPermission(
                         MANAGER1_EMAIL,
                         BAD_COMPANY_ID,
                         NEW_MANAGER_PERMISSIONS,
                         VALID_OWNER1_TOKEN
                         );

                 assertFalse(result.isSuccess());
                 assertEquals(
                         "Production company with ID "
                                 + BAD_COMPANY_ID
                                 + " is not found.",
                         result.getError()
                 );

                 //permissions didn't change
                 ProductionCompany updated =
                         ProductionCompanyRepository.findByID(
                                 String.valueOf(COMPANY1_ID)
                         );

                 assertTrue(
                         updated.hasManagerWithPermissions(
                                 MANAGER1_EMAIL,
                                 ALL_MANAGER_PERMISSIONS
                         )
                 );
         }

        @Test
        void changeManagerPermissions_targetNotManager_fail() {
                Result<Boolean> result =
                        CompanyHierarchyService.changeManagerPermission(
                        BYSTANDER_EMAIL,
                        COMPANY1_ID,
                        NEW_MANAGER_PERMISSIONS,
                        VALID_OWNER1_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals(
                        "Target is not a member.",
                        result.getError()
                );

                //permissions didn't change
                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertFalse(
                        updated.isManager(BAD_USER_EMAIL)
                        );
        }

        @Test
        void updateManagerPermissions_callerNotOwner_fail() {
                Result<Boolean> result =
                        CompanyHierarchyService.changeManagerPermission(
                        MANAGER1_EMAIL,
                        COMPANY1_ID,
                        NEW_MANAGER_PERMISSIONS,
                        VALID_MANAGER2_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals(
                        "Caller is not an owner.",
                        result.getError()
                );

                //permissions didn't change
                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(
                        updated.hasManagerWithPermissions(
                        MANAGER1_EMAIL,
                        ALL_MANAGER_PERMISSIONS
                        )
                );
        }

        @Test
        void updateManagerPermissions_targerIsOwner_fail() {
                Result<Boolean> result =
                        CompanyHierarchyService.changeManagerPermission(
                        OWNER2_EMAIL,
                        COMPANY1_ID,
                        NEW_MANAGER_PERMISSIONS,
                        VALID_OWNER1_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals(
                        "cant update permissions for owner and founder!",
                        result.getError()
                );

                //permissions didn't change
                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(
                        updated.isOwner(OWNER2_EMAIL)
                );
        }

        @Test
        void updateManagerPermissions_emptyPermissions_fail() {
                Result<Boolean> result =
                        CompanyHierarchyService.changeManagerPermission(
                        MANAGER1_EMAIL,
                        COMPANY1_ID,
                        Set.of(),
                        VALID_OWNER1_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals(
                        "permissions can't be null or empty!",
                        result.getError()
                );

                //permissions didn't change
                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(
                        updated.hasManagerWithPermissions(
                                MANAGER1_EMAIL,
                                ALL_MANAGER_PERMISSIONS
                        )
                );
        }

        @Test
        void updateManagerPermissions_nullPermissions_fail() {
                Result<Boolean> result =
                        CompanyHierarchyService.changeManagerPermission(
                        MANAGER1_EMAIL,
                        COMPANY1_ID,
                        null,
                        VALID_OWNER1_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals(
                        "permissions can't be null or empty!",
                        result.getError()
                );

                //permissions didn't change
                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(
                        updated.hasManagerWithPermissions(
                                MANAGER1_EMAIL,
                                ALL_MANAGER_PERMISSIONS
                        )
                );
        }

        @Test
        void updateManagerPermissions_managerNotAssignedByOwner_fail() {
                Result<Boolean> result =
                        CompanyHierarchyService.changeManagerPermission(
                        MANAGER1_EMAIL,
                        COMPANY1_ID,
                        NEW_MANAGER_PERMISSIONS,
                        VALID_OWNER2_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals(
                        "Target was not assigned by this owner (directly or transitively).",
                        result.getError()
                );

                //permissions didn't change
                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                        );

                assertTrue(
                        updated.hasManagerWithPermissions(
                                MANAGER1_EMAIL,
                                ALL_MANAGER_PERMISSIONS
                        )
                );
        }

        @Test
        void changeManagerPermissions_samePermissions_isIdempotent() {
                Result<Boolean> result =
                        CompanyHierarchyService.changeManagerPermission(
                        MANAGER1_EMAIL,
                        COMPANY1_ID,
                        ALL_MANAGER_PERMISSIONS,
                        VALID_OWNER1_TOKEN
                );

                assertTrue(result.isSuccess());

                ProductionCompany updated =
                        ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                );

                assertTrue(
                        updated.hasManagerWithPermissions(
                                MANAGER1_EMAIL,
                                ALL_MANAGER_PERMISSIONS
                        )
                );
        }

        @Test
        void concurrentChangeManagerPermissions_bothSucceed()
                throws Exception {

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        Set<ManagerPermissions> permsA = Set.of(ManagerPermissions.CUSTOMER_SUPPORT, ManagerPermissions.EVENT_INVENTORY);
        Set<ManagerPermissions> permsB = Set.of(ManagerPermissions.PURCHASE_POLICY);

        Callable<Result<Boolean>> task1 = () -> {
                startLatch.await();
                return CompanyHierarchyService.changeManagerPermission(
                        MANAGER1_EMAIL,
                        COMPANY1_ID,
                        permsA,
                        VALID_OWNER1_TOKEN
                );
        };

        Callable<Result<Boolean>> task2 = () -> {
                startLatch.await();
                return CompanyHierarchyService.changeManagerPermission(
                        MANAGER1_EMAIL,
                        COMPANY1_ID,
                        permsB,
                        VALID_OWNER1_TOKEN
                );
        };

        Future<Result<Boolean>> future1 = executor.submit(task1);
        Future<Result<Boolean>> future2 = executor.submit(task2);

        startLatch.countDown();

        Result<Boolean> result1 = future1.get();
        Result<Boolean> result2 = future2.get();

        assertTrue(result1.isSuccess());
        assertTrue(result2.isSuccess());

        ProductionCompany updated =
                ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                );

        // final state must be one of the two valid outcomes
        boolean isA = updated.hasManagerWithPermissions(MANAGER1_EMAIL, permsA);
        boolean isB = updated.hasManagerWithPermissions(MANAGER1_EMAIL, permsB);

        assertTrue(isA || isB);

        executor.shutdown();
        }

        @Test
        void concurrentRemoveManager_andChangePermissions_consistentResult()
                throws Exception {

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        Set<ManagerPermissions> NEW_PERMS = Set.of(ManagerPermissions.CUSTOMER_SUPPORT, ManagerPermissions.EVENT_INVENTORY);

        Callable<Result<Boolean>> removeTask = () -> {
                startLatch.await();
                return CompanyHierarchyService.removeOwnerManager(
                        MANAGER1_EMAIL,
                        COMPANY1_ID,
                        VALID_OWNER1_TOKEN
                );
        };

        Callable<Result<Boolean>> updatePermTask = () -> {
                startLatch.await();
                return CompanyHierarchyService.changeManagerPermission(
                        MANAGER1_EMAIL,
                        COMPANY1_ID,
                        NEW_PERMS,
                        VALID_OWNER1_TOKEN
                );
        };

        Future<Result<Boolean>> removeFuture =
                executor.submit(removeTask);

        Future<Result<Boolean>> updateFuture =
                executor.submit(updatePermTask);

        startLatch.countDown();

        Result<Boolean> removeResult = removeFuture.get();
        Result<Boolean> updateResult = updateFuture.get();

        ProductionCompany updated =
                ProductionCompanyRepository.findByID(
                        String.valueOf(COMPANY1_ID)
                );

        // --- Both outcomes must be safe (no partial corruption)
        assertTrue(removeResult.isSuccess() || updateResult.isSuccess());

        // CASE 1: manager got removed
        if (!updated.isManager(MANAGER1_EMAIL)) {

                // must be fully removed
                assertFalse(
                        updated.isDirectSubordinate(
                                OWNER1_EMAIL,
                                MANAGER1_EMAIL
                        )
                );
        }
        else {
                // CASE 2: manager still exists → permissions must be valid
                assertTrue(
                        updated.hasManagerWithPermissions(
                                MANAGER1_EMAIL,
                                NEW_PERMS
                        )
                );
        }
        executor.shutdown();
        }

        @Test
        void hierarchyTree_success_returnsFullCorrectTree() {

                Result<List<HierarchyNodeDTO>> result =
                        CompanyHierarchyService.hierarchyTree(
                                COMPANY1_ID,
                                VALID_OWNER1_TOKEN
                        );

                assertTrue(result.isSuccess());

                List<HierarchyNodeDTO> tree = result.getValue();

                assertEquals(5, tree.size());

                // founder → owner1
                assertTrue(tree.stream().anyMatch(n ->
                        n.getUserId().equals(OWNER1_EMAIL) &&
                        n.getAssignerId().equals(FOUNDER_EMAIL) &&
                        company1.isOwner(n.getUserId())
                ));

                // owner1 → owner2
                assertTrue(tree.stream().anyMatch(n ->
                        n.getUserId().equals(OWNER2_EMAIL) &&
                        n.getAssignerId().equals(OWNER1_EMAIL) &&
                        company1.isOwner(n.getUserId())
                ));

                // owner1 → manager1
                assertTrue(tree.stream().anyMatch(n ->
                        n.getUserId().equals(MANAGER1_EMAIL) &&
                        n.getAssignerId().equals(OWNER1_EMAIL) &&
                        company1.isManager(n.getUserId())
                ));

                // owner2 → manager2
                assertTrue(tree.stream().anyMatch(n ->
                        n.getUserId().equals(MANAGER2_EMAIL) &&
                        n.getAssignerId().equals(OWNER2_EMAIL) &&
                        company1.isManager(n.getUserId())
                ));

                assertTrue(tree.stream().anyMatch(n ->
                        n.getUserId().equals(FOUNDER_EMAIL) &&
                        n.getAssignerId()==null &&
                        company1.isFounder(n.getUserId())
                ));
        }

        @Test
        void hierarchyTree_nonOwner_fails() {

                Result<List<HierarchyNodeDTO>> result =
                        CompanyHierarchyService.hierarchyTree(
                                COMPANY1_ID,
                                VALID_MANAGER1_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals("Requester is not owner.", result.getError());
        }

        @Test
        void hierarchyTree_companyNotFound_fails() {

                Result<List<HierarchyNodeDTO>> result =
                        CompanyHierarchyService.hierarchyTree(
                                BAD_COMPANY_ID,
                                VALID_OWNER1_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals(
                        "Production company with ID "
                        + BAD_COMPANY_ID
                        + " is not found.",
                        result.getError()
                );
        }

        @Test
        //includes user not found
        void hierarchyTree_staleUser_fails() {
                Result<List<HierarchyNodeDTO>> result =
                        CompanyHierarchyService.hierarchyTree(
                                COMPANY1_ID,
                                STALE_USER_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals(
                        "User with ID " + BAD_USER_EMAIL + " not found.",
                        result.getError()
                );
        }
        @Test
        void hierarchyTree_invalidToken_fails() {

                Result<List<HierarchyNodeDTO>> result =
                        CompanyHierarchyService.hierarchyTree(
                                COMPANY1_ID,
                                INVALID_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals("Invalid Token", result.getError());
        }

        @Test
        void hierarchyTree_afterMutations_reflectsCorrectStructure() {

                // remove a manager first
                CompanyHierarchyService.removeOwnerManager(
                        MANAGER1_EMAIL,
                        COMPANY1_ID,
                        VALID_OWNER1_TOKEN
                );

                Result<List<HierarchyNodeDTO>> result =
                        CompanyHierarchyService.hierarchyTree(
                                COMPANY1_ID,
                                VALID_OWNER1_TOKEN
                        );

                assertTrue(result.isSuccess());

                List<HierarchyNodeDTO> tree = result.getValue();

                // manager1 must be gone
                assertTrue(tree.stream().noneMatch(n ->
                        n.getUserId().equals(MANAGER1_EMAIL)
                ));

                // owner1 still exists
                assertTrue(tree.stream().anyMatch(n ->
                        n.getUserId().equals(OWNER1_EMAIL)
                ));
        }





}