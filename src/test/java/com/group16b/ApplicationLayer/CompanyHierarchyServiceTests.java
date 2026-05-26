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
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
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
        private final String STALE_USER_TOKEN="stale-user-token";

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
                assignManager(company1, OWNER1_EMAIL, MANAGER1_EMAIL, EnumSet.of(ManagerPermissions.CUSTOMER_SUPPORT));
                assignManager(company1, OWNER2_EMAIL, MANAGER2_EMAIL, EnumSet.of(ManagerPermissions.PURCHASE_POLICY));

                company1.AssignManager(OWNER1_EMAIL, INVITED_MANAGER_EMAIL, EnumSet.allOf(ManagerPermissions.class));
                company1.AssignOwner(OWNER2_EMAIL,INVITED_OWNER_EMAIL);

                ProductionCompanyRepository.save(company1);
                ProductionCompanyRepository.save(company2);
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

                assertTrue(updated.hasPendingInvite(BYSTANDER_EMAIL));
        }

        @Test
        void assignOwnerToCompany_invalidToken_fails() {
                assertFalse(company1.hasPendingInvite(BYSTANDER_EMAIL));
                
                Result<Boolean> result =
                        CompanyHierarchyService.assignOwnerToCompany(
                        COMPANY1_ID,
                        BYSTANDER_EMAIL,
                        INVALID_TOKEN
                        );

                assertFalse(result.isSuccess());
                assertEquals("Invalid Token", result.getError());

                ProductionCompany updated =ProductionCompanyRepository.findByID(String.valueOf(COMPANY1_ID));
                assertFalse(updated.hasPendingInvite(BYSTANDER_EMAIL));
        }

        @Test
        void assignOwnerToCompany_staleUser_fails() {
                assertFalse(company1.hasPendingInvite(BYSTANDER_EMAIL));
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

                ProductionCompany updated =ProductionCompanyRepository.findByID(String.valueOf(COMPANY1_ID));
                assertFalse(updated.hasPendingInvite(BYSTANDER_EMAIL));
        }


}