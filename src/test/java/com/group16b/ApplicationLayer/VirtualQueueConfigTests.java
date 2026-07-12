package com.group16b.ApplicationLayer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.ILocationService;
import com.group16b.DomainLayer.DomainServices.EventFilteringService;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.DomainLayer.VirtualQueue.VirtualQueue;
import com.group16b.InfrastructureLayer.ExternalSystems.WsepClient;
import com.group16b.InfrastructureLayer.IdGenerators.ProductionCompanyIdGen;

class VirtualQueueConfigTests {

    @Test
    void validVirtualQueuePassNumDoesNotCrashServices() {
        assertDoesNotThrow(() -> {
            createEventService(50);
            createStartupService(50);
        });
    }

    @Test
    void invalidVirtualQueuePassNumCrashesServices() {
        assertThrows(IllegalArgumentException.class, () -> createEventService(0));
        assertThrows(IllegalArgumentException.class, () -> createStartupService(0));
    }

    @SuppressWarnings("unchecked")
    private EventService createEventService(int virtualQueuePassNum) {
        return new EventService(
                mock(IAuthenticationService.class),
                mock(ILocationService.class),
                mock(EventFilteringService.class),
                mock(IProductionCompanyRepository.class),
                mock(IRepository.class),
                mock(IRepository.class),
                mock(IEventRepository.class),
                mock(IRepository.class),
                virtualQueuePassNum);
    }

    @SuppressWarnings("unchecked")
    private StartupService createStartupService(int virtualQueuePassNum) {
        return new StartupService(
                mock(IRepository.class),
                mock(WsepClient.class),
                mock(IEventRepository.class),
                mock(IRepository.class),
                mock(IProductionCompanyRepository.class),
                mock(ProductionCompanyIdGen.class),
                "admin123",
                "password",
                "mail@example.com",
                virtualQueuePassNum);
    }
}