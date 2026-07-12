package com.group16b.ApplicationLayer;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.DiscountPolicyDTO;
import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.MaxTicketsDiscountDTO;
import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.SimpleDiscountDTO;
import com.group16b.ApplicationLayer.DTOs.DiscountPolicy.SumDiscountDTO;
import com.group16b.ApplicationLayer.Enums.DiscountPolicyTypes;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.DiscountPolicyRecord;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Policies.DiscountPolicy.AndDiscount;
import com.group16b.DomainLayer.Policies.DiscountPolicy.DiscountContext;
import com.group16b.DomainLayer.Policies.DiscountPolicy.DiscountPolicy;
import com.group16b.DomainLayer.Policies.DiscountPolicy.MaxDiscount;
import com.group16b.DomainLayer.Policies.DiscountPolicy.SimpleDiscount;
import com.group16b.DomainLayer.Policies.DiscountPolicy.SumDiscount;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Order.IOrderRepository;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DiscountPolicyServiceTests {

    private IAuthenticationService authenticationService;
    private IProductionCompanyRepository companyRepository;
    private IEventRepository eventRepository;
    private IRepository<User> userRepository;

    private ProductionCompany company;
    private Event event;

    private DiscountPolicyService service;
    @Mock
    private IOrderRepository orderRepository;

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

        service = new DiscountPolicyService(
                authenticationService,
                companyRepository,
                eventRepository,
                userRepository, orderRepository);

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

        when(company.getDiscountPolicy())
                .thenReturn(new HashSet<>());

        when(event.getEventDiscountPolicy())
                .thenReturn(new HashSet<>());
    }

    @Test
    public void createCompanyDiscountPolicy_recursiveAnd_buildsAndSaves() {
        LocalDateTime tomorrow =
                LocalDateTime.now().plusDays(1);

        DiscountPolicyRecord record = and(
                minTickets(2, 10),
                maxDate(tomorrow, 20),
                30);

        Result<Boolean> result =
                service.createCompanyDiscountPolicy(
                        "token",
                        1,
                        record);

        assertTrue(result.isSuccess());

        ArgumentCaptor<DiscountPolicy> captor =
                ArgumentCaptor.forClass(DiscountPolicy.class);

        verify(company).addDiscountPolicy(captor.capture());
        verify(companyRepository).save(company);

        AndDiscount discount = assertInstanceOf(
                AndDiscount.class,
                captor.getValue());

        DiscountContext matchingContext =
                new DiscountContext(
                        20,
                        3,
                        LocalDateTime.now(),
                        null);

        assertEquals(
                70.0,
                discount.calculateDiscount(
                        100.0,
                        matchingContext),
                0.001);

        DiscountContext failingContext =
                new DiscountContext(
                        20,
                        1,
                        LocalDateTime.now(),
                        null);

        assertEquals(
                100.0,
                discount.calculateDiscount(
                        100.0,
                        failingContext),
                0.001);
    }

    @Test
    public void createEventDiscountPolicy_sum_buildsAndSaves() {
        DiscountPolicyRecord record =
                sum(simple(10), simple(20));

        Result<Boolean> result =
                service.createEventDiscountPolicy(
                        "token",
                        7,
                        record);

        assertTrue(result.isSuccess());

        ArgumentCaptor<DiscountPolicy> captor =
                ArgumentCaptor.forClass(DiscountPolicy.class);

        verify(event).addEventDiscountPolicy(captor.capture());
        verify(eventRepository).save(event);

        SumDiscount discount = assertInstanceOf(
                SumDiscount.class,
                captor.getValue());

        assertEquals(
                72.0,
                discount.calculateDiscount(100.0),
                0.001);
    }

    @Test
    public void createCompanyDiscountPolicy_existingPolicy_returnsFailure() {
        when(company.getDiscountPolicy())
                .thenReturn(Set.of(new SimpleDiscount(10)));

        Result<Boolean> result =
                service.createCompanyDiscountPolicy(
                        "token",
                        1,
                        simple(20));

        assertFalse(result.isSuccess());

        verify(company, never())
                .addDiscountPolicy(any());

        verify(companyRepository, never())
                .save(any());
    }

    @Test
    public void editCompanyDiscountPolicy_replacesExistingPolicy() {
        DiscountPolicy oldPolicy =
                new SimpleDiscount(5);

        when(company.getDiscountPolicy())
                .thenReturn(Set.of(oldPolicy));

        Result<Boolean> result =
                service.editCompanyDiscountPolicy(
                        "token",
                        1,
                        max(simple(10), simple(30)));

        assertTrue(result.isSuccess());

        verify(company).removeDiscountPolicy(oldPolicy);

        ArgumentCaptor<DiscountPolicy> captor =
                ArgumentCaptor.forClass(DiscountPolicy.class);

        verify(company).addDiscountPolicy(captor.capture());
        verify(companyRepository).save(company);

        MaxDiscount discount = assertInstanceOf(
                MaxDiscount.class,
                captor.getValue());

        assertEquals(
                70.0,
                discount.calculateDiscount(100.0),
                0.001);
    }

    @Test
    public void editEventDiscountPolicy_nullRecord_deletesPolicy() {
        DiscountPolicy oldPolicy =
                new SimpleDiscount(10);

        when(event.getEventDiscountPolicy())
                .thenReturn(Set.of(oldPolicy));

        Result<Boolean> result =
                service.editEventDiscountPolicy(
                        "token",
                        7,
                        null);

        assertTrue(result.isSuccess());

        verify(event).removeEventDiscountPolicy(oldPolicy);

        verify(event, never())
                .addEventDiscountPolicy(any());

        verify(eventRepository).save(event);
    }

    @Test
    public void getEventDiscountPolicy_recursivePolicy_returnsDTO() {
        when(event.getEventDiscountPolicy())
                .thenReturn(Set.of(
                        new SumDiscount(
                                new SimpleDiscount(10),
                                new com.group16b.DomainLayer.Policies.DiscountPolicy.AmountRangeDiscount(
                                        null,
                                        4,
                                        20))));

        Result<DiscountPolicyDTO> result =
                service.getEventDiscountPolicy(
                        "token",
                        7);

        assertTrue(result.isSuccess());

        SumDiscountDTO dto = assertInstanceOf(
                SumDiscountDTO.class,
                result.getValue());

        assertInstanceOf(
                SimpleDiscountDTO.class,
                dto.getLeft());

        assertInstanceOf(
                MaxTicketsDiscountDTO.class,
                dto.getRight());
    }

    @Test
    public void createCompanyDiscountPolicy_invalidToken_returnsFailure() {
        when(authenticationService.validateToken("bad"))
                .thenReturn(false);

        Result<Boolean> result =
                service.createCompanyDiscountPolicy(
                        "bad",
                        1,
                        simple(10));

        assertFalse(result.isSuccess());

        verifyNoInteractions(
                companyRepository,
                eventRepository,
                userRepository);
    }

    @Test
    public void createCompanyDiscountPolicy_missingPercentage_returnsFailure() {
        DiscountPolicyRecord invalidRecord =
                new DiscountPolicyRecord(
                        DiscountPolicyTypes.SIMPLE,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

        Result<Boolean> result =
                service.createCompanyDiscountPolicy(
                        "token",
                        1,
                        invalidRecord);

        assertFalse(result.isSuccess());

        verify(companyRepository, never()).save(any());
    }

    @Test
    public void createCompanyDiscountPolicy_couponType_returnsFailure() {
        DiscountPolicyRecord coupon =
                new DiscountPolicyRecord(
                        DiscountPolicyTypes.COUPON,
                        10.0,
                        null,
                        null,
                        null,
                        null,
                        "SAVE10",
                        LocalDateTime.now().plusDays(1),
                        5,
                        null,
                        null);

        Result<Boolean> result =
                service.createCompanyDiscountPolicy(
                        "token",
                        1,
                        coupon);

        assertFalse(result.isSuccess());

        verify(companyRepository, never()).save(any());
    }

    private DiscountPolicyRecord simple(double percentage) {
        return new DiscountPolicyRecord(
                DiscountPolicyTypes.SIMPLE,
                percentage,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private DiscountPolicyRecord minTickets(
            int minimum,
            double percentage) {

        return new DiscountPolicyRecord(
                DiscountPolicyTypes.MIN_TICKETS,
                percentage,
                minimum,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private DiscountPolicyRecord maxDate(
            LocalDateTime endDate,
            double percentage) {

        return new DiscountPolicyRecord(
                DiscountPolicyTypes.MAX_DATE,
                percentage,
                null,
                null,
                null,
                endDate,
                null,
                null,
                null,
                null,
                null);
    }

    private DiscountPolicyRecord and(
            DiscountPolicyRecord left,
            DiscountPolicyRecord right,
            double percentage) {

        return new DiscountPolicyRecord(
                DiscountPolicyTypes.AND,
                percentage,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                left,
                right);
    }

    private DiscountPolicyRecord sum(
            DiscountPolicyRecord left,
            DiscountPolicyRecord right) {

        return new DiscountPolicyRecord(
                DiscountPolicyTypes.SUM,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                left,
                right);
    }

    private DiscountPolicyRecord max(
            DiscountPolicyRecord left,
            DiscountPolicyRecord right) {

        return new DiscountPolicyRecord(
                DiscountPolicyTypes.MAX,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                left,
                right);
    }
}