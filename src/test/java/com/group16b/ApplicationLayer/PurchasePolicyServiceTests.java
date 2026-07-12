package com.group16b.ApplicationLayer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.AndDTO;
import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.MaxTicketsDTO;
import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.MinAgeDTO;
import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.OrDTO;
import com.group16b.ApplicationLayer.DTOs.PurchasePolicy.PurchasePolicyDTO;
import com.group16b.ApplicationLayer.Enums.PurchasePolicyTypes;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.PurchasePolicyRecord;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Policies.PurchasePolicy.AgePolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.AndPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.MaxTicketsPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.MinTicketsPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.OrPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchaseContext;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicyException;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.User.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PurchasePolicyServiceTests {

    private IAuthenticationService authenticationService;
    private IProductionCompanyRepository companyRepository;
    private IEventRepository eventRepository;
    private IRepository<User> userRepository;

    private ProductionCompany company;
    private Event event;

    private PurchasePolicyService service;

    @BeforeEach
    public void setUp() {
        authenticationService =
                mock(IAuthenticationService.class);

        companyRepository =
                mock(IProductionCompanyRepository.class);

        eventRepository =
                mock(IEventRepository.class);

        userRepository =
                mock(IRepository.class);

        company = mock(ProductionCompany.class);
        event = mock(Event.class);

        service = new PurchasePolicyService(
                authenticationService,
                companyRepository,
                eventRepository,
                userRepository);

        when(authenticationService.validateToken("token"))
                .thenReturn(true);

        when(authenticationService.isUserToken("token"))
                .thenReturn(true);

        when(authenticationService.extractSubjectFromToken("token"))
                .thenReturn("user@example.com");

        when(companyRepository.findByID("1"))
                .thenReturn(company);

        when(eventRepository.findByID("7"))
                .thenReturn(event);

        when(event.getEventProductionCompanyID())
                .thenReturn(1);

        when(company.getPurchasePolicy())
                .thenReturn(new HashSet<>());

        when(event.getEventPurchasePolicy())
                .thenReturn(new HashSet<>());
    }

    @Test
    public void createCompanyPurchasePolicy_recursiveAnd_buildsAndSaves() {
        PurchasePolicyRecord record = new PurchasePolicyRecord(
                PurchasePolicyTypes.AND,
                null,
                null,
                null,
                null,
                minAge(18),
                maxTickets(4));

        Result<Boolean> result =
                service.createCompanyPurchasePolicy(
                        "token",
                        1,
                        record);

        assertTrue(result.isSuccess());

        ArgumentCaptor<PurchasePolicy> captor =
                ArgumentCaptor.forClass(PurchasePolicy.class);

        verify(company).addPurchasePolicy(captor.capture());
        verify(companyRepository).save(company);

        AndPolicy policy = assertInstanceOf(
                AndPolicy.class,
                captor.getValue());

        assertDoesNotThrow(
                () -> policy.validatePurchase(
                        new PurchaseContext(25, 3)));

        assertThrows(
                PurchasePolicyException.class,
                () -> policy.validatePurchase(
                        new PurchaseContext(17, 3)));

        assertThrows(
                PurchasePolicyException.class,
                () -> policy.validatePurchase(
                        new PurchaseContext(25, 5)));
    }

    @Test
    public void createEventPurchasePolicy_recursiveOr_buildsAndSaves() {
        PurchasePolicyRecord record = new PurchasePolicyRecord(
                PurchasePolicyTypes.OR,
                null,
                null,
                null,
                null,
                maxAge(30),
                minTickets(3));

        Result<Boolean> result =
                service.createEventPurchasePolicy(
                        "token",
                        7,
                        record);

        assertTrue(result.isSuccess());

        ArgumentCaptor<PurchasePolicy> captor =
                ArgumentCaptor.forClass(PurchasePolicy.class);

        verify(event).addEventPurchasePolicy(captor.capture());
        verify(eventRepository).save(event);

        OrPolicy policy = assertInstanceOf(
                OrPolicy.class,
                captor.getValue());

        assertDoesNotThrow(
                () -> policy.validatePurchase(
                        new PurchaseContext(25, 1)));

        assertDoesNotThrow(
                () -> policy.validatePurchase(
                        new PurchaseContext(40, 3)));

        assertThrows(
                PurchasePolicyException.class,
                () -> policy.validatePurchase(
                        new PurchaseContext(40, 1)));
    }

    @Test
    public void createCompanyPurchasePolicy_existingPolicy_returnsFailure() {
        when(company.getPurchasePolicy())
                .thenReturn(Set.of(new MinTicketsPolicy(1)));

        Result<Boolean> result =
                service.createCompanyPurchasePolicy(
                        "token",
                        1,
                        minTickets(2));

        assertFalse(result.isSuccess());

        verify(company, never())
                .addPurchasePolicy(any());

        verify(companyRepository, never())
                .save(any());
    }

    @Test
    public void editCompanyPurchasePolicy_replacesExistingPolicy() {
        PurchasePolicy oldPolicy =
                new MinTicketsPolicy(1);

        when(company.getPurchasePolicy())
                .thenReturn(Set.of(oldPolicy));

        Result<Boolean> result =
                service.editCompanyPurchasePolicy(
                        "token",
                        1,
                        maxAge(65));

        assertTrue(result.isSuccess());

        verify(company).removePurchasePolicy(oldPolicy);

        ArgumentCaptor<PurchasePolicy> captor =
                ArgumentCaptor.forClass(PurchasePolicy.class);

        verify(company).addPurchasePolicy(captor.capture());
        verify(companyRepository).save(company);

        AgePolicy replacement = assertInstanceOf(
                AgePolicy.class,
                captor.getValue());

        assertNull(replacement.getMinAge());
        assertEquals(65, replacement.getMaxAge());
    }

    @Test
    public void editEventPurchasePolicy_nullRecord_deletesPolicy() {
        PurchasePolicy oldPolicy =
                new MaxTicketsPolicy(4);

        when(event.getEventPurchasePolicy())
                .thenReturn(Set.of(oldPolicy));

        Result<Boolean> result =
                service.editEventPurchasePolicy(
                        "token",
                        7,
                        null);

        assertTrue(result.isSuccess());

        verify(event).removeEventPurchasePolicy(oldPolicy);

        verify(event, never())
                .addEventPurchasePolicy(any());

        verify(eventRepository).save(event);
    }

    @Test
    public void getCompanyPurchasePolicy_recursivePolicy_returnsDTO() {
        when(company.getPurchasePolicy())
                .thenReturn(Set.of(
                        new AndPolicy(List.of(
                                new AgePolicy(18, null),
                                new MaxTicketsPolicy(4)))));

        Result<PurchasePolicyDTO> result =
                service.getCompanyPurchasePolicy(
                        "token",
                        1);

        assertTrue(result.isSuccess());

        AndDTO dto = assertInstanceOf(
                AndDTO.class,
                result.getValue());

        assertInstanceOf(MinAgeDTO.class, dto.getLeft());
        assertInstanceOf(MaxTicketsDTO.class, dto.getRight());
    }

    @Test
    public void getEventPurchasePolicy_orPolicy_returnsDTO() {
        when(event.getEventPurchasePolicy())
                .thenReturn(Set.of(
                        new OrPolicy(List.of(
                                new AgePolicy(18, null),
                                new MaxTicketsPolicy(2)))));

        Result<PurchasePolicyDTO> result =
                service.getEventPurchasePolicy(
                        "token",
                        7);

        assertTrue(result.isSuccess());
        assertInstanceOf(OrDTO.class, result.getValue());
    }

    @Test
    public void createCompanyPurchasePolicy_invalidToken_returnsFailure() {
        when(authenticationService.validateToken("bad"))
                .thenReturn(false);

        Result<Boolean> result =
                service.createCompanyPurchasePolicy(
                        "bad",
                        1,
                        minAge(18));

        assertFalse(result.isSuccess());

        verifyNoInteractions(
                companyRepository,
                eventRepository,
                userRepository);
    }

    @Test
    public void createCompanyPurchasePolicy_missingValue_returnsFailure() {
        PurchasePolicyRecord invalidRecord =
                new PurchasePolicyRecord(
                        PurchasePolicyTypes.MIN_AGE,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

        Result<Boolean> result =
                service.createCompanyPurchasePolicy(
                        "token",
                        1,
                        invalidRecord);

        assertFalse(result.isSuccess());

        verify(companyRepository, never()).save(any());
    }

    private PurchasePolicyRecord minAge(int value) {
        return new PurchasePolicyRecord(
                PurchasePolicyTypes.MIN_AGE,
                value,
                null,
                null,
                null,
                null,
                null);
    }

    private PurchasePolicyRecord maxAge(int value) {
        return new PurchasePolicyRecord(
                PurchasePolicyTypes.MAX_AGE,
                null,
                value,
                null,
                null,
                null,
                null);
    }

    private PurchasePolicyRecord minTickets(int value) {
        return new PurchasePolicyRecord(
                PurchasePolicyTypes.MIN_TICKETS,
                null,
                null,
                value,
                null,
                null,
                null);
    }

    private PurchasePolicyRecord maxTickets(int value) {
        return new PurchasePolicyRecord(
                PurchasePolicyTypes.MAX_TICKETS,
                null,
                null,
                null,
                value,
                null,
                null);
    }
}