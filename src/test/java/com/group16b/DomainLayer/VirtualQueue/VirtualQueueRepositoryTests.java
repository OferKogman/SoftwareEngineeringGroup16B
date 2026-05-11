package com.group16b.DomainLayer.VirtualQueue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VirtualQueueRepositoryTests {
    
    VirtualQueueRepository virtualQueueRepository;
    
    @BeforeEach
    void setUp(){
        virtualQueueRepository = VirtualQueueRepository.getInstance();
    }

    @Test
    void AddAndGetQueueById(){
        VirtualQueue vQueue = mock(VirtualQueue.class);
        when(vQueue.getId()).thenReturn(67);
        
        assertDoesNotThrow(() -> virtualQueueRepository.addVirtualQueue(vQueue));
        
        VirtualQueue received = virtualQueueRepository.findVirtualQueueById(vQueue.getId());
        
        assertEquals(vQueue.getId(), received.getId());
    }

    @Test
    void AddVirtualQueueByIdNotExisting(){
        assertThrows(
            IllegalArgumentException.class, 
            () -> virtualQueueRepository.findVirtualQueueById(99)
        );
    }

    @Test
    void AddExistingQueueFail(){
        VirtualQueue vQueue = mock(VirtualQueue.class);
        when(vQueue.getId()).thenReturn(68);
        
        assertDoesNotThrow(() -> virtualQueueRepository.addVirtualQueue(vQueue));
        
        assertThrows(
            IllegalArgumentException.class, 
            () -> virtualQueueRepository.addVirtualQueue(vQueue)
        );     
    }
}