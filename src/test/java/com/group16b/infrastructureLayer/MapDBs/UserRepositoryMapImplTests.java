package com.group16b.infrastructureLayer.MapDBs;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.DomainLayer.User.User;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;

public class UserRepositoryMapImplTests {

    private Map<String, User> users;
    private UserRepositoryMapImpl userRepository;
    
    private User mockUser1;
    private User mockUser2;

    @BeforeEach
    void setUp() {
        users = new HashMap<>();
        userRepository = new UserRepositoryMapImpl(users);

        mockUser1 = mock(User.class);
        when(mockUser1.getEmail()).thenReturn("admin@test.com");
        when(mockUser1.getVersion()).thenReturn(1L);

        mockUser2 = mock(User.class);
        when(mockUser2.getEmail()).thenReturn("guest@test.com");
        when(mockUser2.getVersion()).thenReturn(1L);
    }

    @Test
    void findByID_UserExists_ReturnsUserAndAssertsByEmail() {
        users.put("admin@test.com", mockUser1);

        User result = userRepository.findByID("admin@test.com");

        assertEquals(mockUser1.getEmail(), result.getEmail());
    }

    @Test
    void findByID_UserDoesNotExist_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> userRepository.findByID("missing@test.com"));
            
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void getAll_MapIsEmpty_ReturnsEmptyList() {
        List<User> results = userRepository.getAll();
        assertTrue(results.isEmpty());
    }

    @Test
    void getAll_UsersExist_ReturnsListAssertsByEmails() {
        users.put("admin@test.com", mockUser1);
        users.put("guest@test.com", mockUser2);

        List<User> results = userRepository.getAll();

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(u -> u.getEmail().equals("admin@test.com")));
        assertTrue(results.stream().anyMatch(u -> u.getEmail().equals("guest@test.com")));
    }

    @Test
    void delete_UserExists_RemovesFromMapSuccessfully() {
        users.put("admin@test.com", mockUser1);

        userRepository.delete("admin@test.com");

        assertFalse(users.containsKey("admin@test.com"));
    }

    @Test
    void delete_UserDoesNotExist_ThrowsIllegalArgumentExceptionAndMapUntouched() {
        users.put("admin@test.com", mockUser1); 

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> userRepository.delete("missing@test.com"));

        assertTrue(ex.getMessage().contains("not found"));
        assertEquals(1, users.size()); 
    }


    @Test
    void save_NewUser_AddsToMapAndSetsInitialVersion() {
        when(mockUser1.getVersion()).thenReturn(0L); 

        userRepository.save(mockUser1);

        assertTrue(users.containsKey("admin@test.com"));
        verify(mockUser1, times(1)).setVersion(1L); 
    }

    @Test
    void save_ExistingUser_ValidVersion_UpdatesSuccessfully() {
        users.put("admin@test.com", mockUser1); 
        
        User incomingUser = mock(User.class);
        when(incomingUser.getEmail()).thenReturn("admin@test.com");
        when(incomingUser.getVersion()).thenReturn(1L); 

        userRepository.save(incomingUser);

        verify(mockUser1, times(1)).update(incomingUser); 
    }

    @Test
    void save_ExistingUser_StaleVersion_ThrowsIllegalStateException() {
        users.put("admin@test.com", mockUser1); 
        
        User staleUser = mock(User.class);
        when(staleUser.getEmail()).thenReturn("admin@test.com");
        when(staleUser.getVersion()).thenReturn(0L); 

        IllegalStateException ex = assertThrows(IllegalStateException.class, 
            () -> userRepository.save(staleUser));
        
        assertTrue(ex.getMessage().contains("modified by another process"));
        verify(mockUser1, never()).update(any()); 
    }

    @Test
    void save_ExistingUser_FutureVersion_ThrowsIllegalStateException() {
        users.put("admin@test.com", mockUser1); 
        
        User futureUser = mock(User.class);
        when(futureUser.getEmail()).thenReturn("admin@test.com");
        when(futureUser.getVersion()).thenReturn(2L); 

        IllegalStateException ex = assertThrows(IllegalStateException.class, 
            () -> userRepository.save(futureUser));
        
        assertTrue(ex.getMessage().contains("modified by another process"));
        verify(mockUser1, never()).update(any()); 
    }


@Test
    void save_ConcurrentWritesToSameUser_OptimisticLockPreventsOverwrites() throws InterruptedException {
        AtomicLong dynamicVersion = new AtomicLong(1L);
        
        when(mockUser1.getVersion()).thenAnswer(invocation -> dynamicVersion.get());//checking the current version and not what was it in the beginning
        
        //when update is called, increment the dynamicVersion
        doAnswer(invocation -> {
            dynamicVersion.incrementAndGet();
            return null;
        }).when(mockUser1).update(any(User.class));

        users.put("admin@test.com", mockUser1);
        
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.execute(() -> {
                try {
                    User concurrentMock = mock(User.class);
                    when(concurrentMock.getEmail()).thenReturn("admin@test.com");
                    when(concurrentMock.getVersion()).thenReturn(1L); // All threads think it is v1
                    
                    userRepository.save(concurrentMock); 
                } catch (IllegalStateException e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals(9, failureCount.get(), "9 threads should have been rejected by the Optimistic Lock.");
        verify(mockUser1, times(1)).update(any(User.class));
    }
}