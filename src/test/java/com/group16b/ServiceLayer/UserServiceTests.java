package com.group16b.ServiceLayer;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.group16b.ApplicationLayer.UserService;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.DomainLayer.User.IUserRepository;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.User.Roles.Owner;
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
}
