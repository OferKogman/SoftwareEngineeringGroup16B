package com.group16b.infrastructureLayer;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.group16b.DomainLayer.DomainServices.CompanyHierarchyDomainService;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.Roles.Manager;
import com.group16b.DomainLayer.User.Roles.ManagerPermissions;
import com.group16b.DomainLayer.User.Roles.Owner;
import com.group16b.DomainLayer.User.User;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;

public class CompanyHierarchyDomainServiceTests {

    private CompanyHierarchyDomainService domainService;
    private UserRepositoryMapImpl mockRepo; 
    private final int companyID = 10;

    @BeforeEach
    void setUp() throws Exception {
        mockRepo = mock(UserRepositoryMapImpl.class);
        
        domainService = new CompanyHierarchyDomainService();

        // 3. THE DYNAMIC BRIDGE:
        // This intercepts all internal database calls and dynamically routes them to whatever 
        // getInstance() is currently returning inside your specific test's MockedStatic block.
        IUserRepository bridgeRepo = mock(IUserRepository.class, invocation -> {
            IUserRepository activeRepo = UserRepositoryMapImpl.getInstance();
            if (activeRepo != null) {
                return invocation.getMethod().invoke(activeRepo, invocation.getArguments());
            }
            return null;
        });

        //Inject the bridge using the same Reflection you used in the ApplicationLayer tests
        Field userRepo = domainService.getClass().getDeclaredField("userRepository");
        userRepo.setAccessible(true);
        userRepo.set(domainService, bridgeRepo);
    }

    @Test
    void isManagerUnderOwner_DeepHierarchy_ReturnsTrue() {
        User owner = mock(User.class);      // ID 1
        User managerMid = mock(User.class); // ID 2
        User managerDeep = mock(User.class); // ID 3

        when(owner.getEmail()).thenReturn(1);
        when(owner.isOwnerOfCompany(companyID)).thenReturn(true);

        // Chain: Deep (3) -> Mid (2) -> Owner (1)
        when(managerDeep.getEmail()).thenReturn(3);
        when(managerDeep.getParentIDForCompany(companyID)).thenReturn(2);

        when(managerMid.getEmail()).thenReturn(2);
        when(managerMid.getParentIDForCompany(companyID)).thenReturn(1);

        try (MockedStatic<UserRepositoryMapImpl> mockedStatic = mockStatic(UserRepositoryMapImpl.class)) {
            mockedStatic.when(UserRepositoryMapImpl::getInstance).thenReturn(mockRepo);
            
            // CRITICAL: We must ensure the repo returns managerMid when the loop traverses up
            when(mockRepo.getUserByEmail(2)).thenReturn(managerMid);
            // Also return owner just in case the loop checks the last ID via repo
            when(mockRepo.getUserByEmail(1)).thenReturn(owner);

            boolean result = domainService.isManagerUnderOwnerTreeTraversal(managerDeep, owner, companyID);
            assertTrue(result, "Should find owner at the top of the hierarchy");
        }
    }

    @Test
    void removeUserFromCompany_LogicCheck() {
        User user = mock(User.class);
        User parent = mock(User.class);
        Manager userRole = mock(Manager.class);
        Owner parentRole = mock(Owner.class);

        when(user.getParentIDForCompany(companyID)).thenReturn(5);
        when(user.getRole(companyID)).thenReturn(userRole);
        
        try (MockedStatic<UserRepositoryMapImpl> mockedStatic = mockStatic(UserRepositoryMapImpl.class)) {
            mockedStatic.when(UserRepositoryMapImpl::getInstance).thenReturn(mockRepo);
            when(mockRepo.getUserByEmail(5)).thenReturn(parent);
            when(parent.getRole(companyID)).thenReturn(parentRole);

            domainService.removeUserFromCompany(user, companyID);

            verify(parentRole).removeManager(userRole);
            verify(user).removeRole(companyID);
        }
    }

    @Test
    void isManagerUnderOwner_DirectChild_ReturnsTrue() {
        User owner = mock(User.class);
        User manager = mock(User.class);

        when(owner.getEmail()).thenReturn(1);
        when(owner.isOwnerOfCompany(companyID)).thenReturn(true);
        when(manager.getEmail()).thenReturn(2);
        when(manager.getParentIDForCompany(companyID)).thenReturn(1);

        assertTrue(domainService.isManagerUnderOwnerTreeTraversal(manager, owner, companyID));
    }

    @Test
    void isManagerUnderOwner_SameUser_ReturnsFalse() {
        User owner = mock(User.class);
        when(owner.getEmail()).thenReturn(1);
        when(owner.isOwnerOfCompany(companyID)).thenReturn(true);

        assertFalse(domainService.isManagerUnderOwnerTreeTraversal(owner, owner, companyID));
    }

    @Test
    void updateManagerPermissions_CallsInternalUpdate() {
        User user = mock(User.class);
        Manager mockManagerRole = mock(Manager.class);
        Set<ManagerPermissions> perms = new HashSet<>();
        perms.add(ManagerPermissions.EVENT_INVENTORY);

        when(user.getRole(companyID)).thenReturn(mockManagerRole);

        domainService.updateManagerPermissionsForCompny(user, companyID, perms);

        verify(mockManagerRole).updatePermissions(perms);
    }
}