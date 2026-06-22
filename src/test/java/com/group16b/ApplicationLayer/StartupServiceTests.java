package com.group16b.ApplicationLayer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.ApplicationLayer.Exceptions.SystemStartupException;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;
import com.group16b.InfrastructureLayer.ExternalSystems.WsepClient;

public class StartupServiceTests {
    private StartupService startupService;

    private WsepClient mockWsepClient;
    private IRepository<SystemAdmin> mockAdminRepository;

    private final List<SystemAdmin> INITIAL_ADMINS=List.of(new SystemAdmin());

    @BeforeEach
    void startup()
    {
        mockWsepClient=mock(WsepClient.class);
        mockAdminRepository=mock(IRepository.class);
        startupService=new StartupService(mockAdminRepository, mockWsepClient);

        doNothing().when(mockWsepClient).handshake();
        when(mockAdminRepository.getAll()).thenReturn(INITIAL_ADMINS);
    }

    @Test
    void givenAllConditionsSatisfied_whenInitializeSystem_success()
    {
        assertDoesNotThrow(()->startupService.initializeSystem());
        verify(mockWsepClient).handshake();
        verify(mockAdminRepository).getAll();
        verify(mockAdminRepository,never()).save(any());
    }

    @Test
    void givenEmptyAdminList_whenInitializeSystem_createNewAdminAndsuccess()
    {
        when(mockAdminRepository.getAll()).thenReturn(new ArrayList<SystemAdmin>());
        assertDoesNotThrow(()->startupService.initializeSystem());
        verify(mockWsepClient).handshake();
        verify(mockAdminRepository).getAll();
        verify(mockAdminRepository).save(any());
    }

    @Test
    void givenFaultyRepo_whenInitializeSystem_Throw()
    {
        doThrow(new RuntimeException("muhaha")).when(mockAdminRepository).getAll();
        SystemStartupException e= assertThrows(SystemStartupException.class,()->startupService.initializeSystem());
        verify(mockWsepClient,never()).handshake();
        verify(mockAdminRepository).getAll();
        verify(mockAdminRepository,never()).save(any());
        assertEquals("Failed to initialize system admins.", e.getMessage());
    }

    @Test
    void givenEmptyAdminListAndFaultyRepo_whenInitializeSystem_createNewAdminAndThrowOnSave()
    {
        when(mockAdminRepository.getAll()).thenReturn(new ArrayList<SystemAdmin>());
        doThrow(new RuntimeException("muhaha")).when(mockAdminRepository).save(any());
        SystemStartupException e= assertThrows(SystemStartupException.class,()->startupService.initializeSystem());
        verify(mockWsepClient,never()).handshake();
        verify(mockAdminRepository).getAll();
        verify(mockAdminRepository).save(any());
        assertEquals("Failed to initialize system admins.", e.getMessage());
    }
}
