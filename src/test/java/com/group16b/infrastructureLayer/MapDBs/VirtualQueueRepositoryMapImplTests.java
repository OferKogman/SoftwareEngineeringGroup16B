package com.group16b.infrastructureLayer.MapDBs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.DomainLayer.VirtualQueue.VirtualQueue;
import com.group16b.InfrastructureLayer.MapDBs.VirtualQueueRepositoryMapImpl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VirtualQueueRepositoryMapImplTests {
    
    VirtualQueueRepositoryMapImpl virtualQueueRepository;
    VirtualQueue vQueue;
    
    @BeforeEach
    void setUp(){
        virtualQueueRepository = new VirtualQueueRepositoryMapImpl();
        vQueue = new VirtualQueue(1);
    }

    @Test
    void AddAndGetQueueById(){
        assertDoesNotThrow(() -> virtualQueueRepository.save(vQueue));
        
        VirtualQueue received = virtualQueueRepository.findByID(Integer.toString(vQueue.getId()));
        
        assertEquals(vQueue.getId(), received.getId());
    }

    @Test
    void AddVirtualQueueByIdNotExisting(){
        assertThrows(
            IllegalArgumentException.class, 
            () -> virtualQueueRepository.findByID("99")
        );
    }
}