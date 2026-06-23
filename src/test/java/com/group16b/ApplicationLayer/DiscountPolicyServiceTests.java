package com.group16b.ApplicationLayer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.ApplicationLayer.DTOs.DiscountPolicyDTO;
import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.DiscountPolicyRecord;
import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.User.User;
import com.group16b.DomainLayer.Venue.Entrance;
import com.group16b.DomainLayer.Venue.FieldSeg;
import com.group16b.DomainLayer.Venue.GridRectangle;
import com.group16b.DomainLayer.Venue.Location;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Stage;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.DomainLayer.Venue.VenueGrid;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.ProductionCompanyRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.VenueRepositoryMapImpl;
import com.group16b.InfrastructureLayer.Security.Role;

public class DiscountPolicyServiceTests {

    private DiscountPolicyService discountPolicyService;
    private IAuthenticationService mockTokenService;
    private IProductionCompanyRepository productionCompanyRepository;
    private IRepository<User> userRepository;
    private IEventRepository eventRepository;

    private User user;
    private User user2;
    private Event e1;
    private ProductionCompany company;

    // Simple record helpers — null out fields we don't need
    private DiscountPolicyRecord simple(double pct) {
        return new DiscountPolicyRecord("SIMPLE", pct, null, null, null, null, null, null, null, null);
    }

    private DiscountPolicyRecord amountRange(Integer min, Integer max, double pct) {
        return new DiscountPolicyRecord("AMOUNT_RANGE", pct, min, max, null, null, null, null, null, null);
    }

    private DiscountPolicyRecord coupon(double pct, String code, LocalDateTime expiry, Integer maxUsages) {
        return new DiscountPolicyRecord("COUPON", pct, null, null, null, null, code, expiry, maxUsages, null);
    }

    private DiscountPolicyRecord and(double pct, DiscountPolicyRecord... children) {
        return new DiscountPolicyRecord("AND", pct, null, null, null, null, null, null, null, List.of(children));
    }

    private DiscountPolicyRecord or(double pct, DiscountPolicyRecord... children) {
        return new DiscountPolicyRecord("OR", pct, null, null, null, null, null, null, null, List.of(children));
    }

    @BeforeEach
    public void setUp() {
        mockTokenService = mock(IAuthenticationService.class);
        productionCompanyRepository = new ProductionCompanyRepositoryMapImpl();
        eventRepository = new EventRepositoryMapImpl();
        userRepository = new UserRepositoryMapImpl();

        discountPolicyService = new DiscountPolicyService(
                mockTokenService, productionCompanyRepository, eventRepository, userRepository);

        // Auth mocks
        when(mockTokenService.validateToken("invalid_token")).thenReturn(false);
        when(mockTokenService.validateToken("guest")).thenReturn(true);
        when(mockTokenService.isUserToken("guest")).thenReturn(false);

        user = new User("testuser", "password");
        userRepository.save(user);
        when(mockTokenService.validateToken("user1")).thenReturn(true);
        when(mockTokenService.isUserToken("user1")).thenReturn(true);
        when(mockTokenService.extractRoleFromToken("user1")).thenReturn(Role.SIGNED);
        when(mockTokenService.extractSubjectFromToken("user1")).thenReturn(user.getEmail());

        user2 = new User("testuser2", "password");
        userRepository.save(user2);
        when(mockTokenService.validateToken("user2")).thenReturn(true);
        when(mockTokenService.isUserToken("user2")).thenReturn(true);
        when(mockTokenService.extractRoleFromToken("user2")).thenReturn(Role.SIGNED);
        when(mockTokenService.extractSubjectFromToken("user2")).thenReturn(user2.getEmail());

        // Company — user is owner (has all permissions including DISCOUNT_POLICY)
        company = new ProductionCompany(1, "Pixar", 3.5, "testuser");
        productionCompanyRepository.save(company);

        // Event — belongs to company 1
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(2);
        e1 = new Event(
                new EventRecord("venue1", "event1", startTime, endTime, "artist1", "category1", 1, 3.5),
                user.getEmail());
        e1.activateEvent();
        eventRepository.save(e1);
    }

    // ================================================================
    // createCompanyDiscountPolicy
    // ================================================================

    @Test
    public void createCompanyDiscountPolicy_SimpleDiscount_Success() {
        Result<Boolean> res = discountPolicyService.createCompanyDiscountPolicy(
                "user1", company.getProductionCompanyID(), simple(10));
        assertTrue(res.isSuccess());
    }

    @Test
    public void createCompanyDiscountPolicy_AmountRangeDiscount_Success() {
        Result<Boolean> res = discountPolicyService.createCompanyDiscountPolicy(
                "user1", company.getProductionCompanyID(), amountRange(2, 5, 15));
        assertTrue(res.isSuccess());
    }

    @Test
    public void createCompanyDiscountPolicy_CouponDiscount_Success() {
        Result<Boolean> res = discountPolicyService.createCompanyDiscountPolicy(
                "user1", company.getProductionCompanyID(),
                coupon(20, "SAVE20", LocalDateTime.now().plusDays(30), 100));
        assertTrue(res.isSuccess());
    }

    @Test
    public void createCompanyDiscountPolicy_AndDiscount_Success() {
        // AND of two simple children
        Result<Boolean> res = discountPolicyService.createCompanyDiscountPolicy(
                "user1", company.getProductionCompanyID(),
                and(25, simple(0), amountRange(2, null, 0)));
        assertTrue(res.isSuccess());
    }

    @Test
    public void createCompanyDiscountPolicy_OrDiscount_Success() {
        Result<Boolean> res = discountPolicyService.createCompanyDiscountPolicy(
                "user1", company.getProductionCompanyID(),
                or(15, simple(0), amountRange(null, 5, 0)));
        assertTrue(res.isSuccess());
    }

    @Test
    public void createCompanyDiscountPolicy_FailInvalidToken() {
        Result<Boolean> res = discountPolicyService.createCompanyDiscountPolicy(
                "invalid_token", company.getProductionCompanyID(), simple(10));
        assertFalse(res.isSuccess());
    }

    @Test
    public void createCompanyDiscountPolicy_FailGuestToken() {
        Result<Boolean> res = discountPolicyService.createCompanyDiscountPolicy(
                "guest", company.getProductionCompanyID(), simple(10));
        assertFalse(res.isSuccess());
    }

    @Test
    public void createCompanyDiscountPolicy_FailNoPermission() {
        // user2 is not a member of the company
        Result<Boolean> res = discountPolicyService.createCompanyDiscountPolicy(
                "user2", company.getProductionCompanyID(), simple(10));
        assertFalse(res.isSuccess());
    }

    @Test
    public void createCompanyDiscountPolicy_FailCompanyNotFound() {
        Result<Boolean> res = discountPolicyService.createCompanyDiscountPolicy(
                "user1", 999, simple(10));
        assertFalse(res.isSuccess());
    }

    @Test
    public void createCompanyDiscountPolicy_FailInvalidRecord_NullType() {
        DiscountPolicyRecord bad = new DiscountPolicyRecord(
                null, 10.0, null, null, null, null, null, null, null, null);
        Result<Boolean> res = discountPolicyService.createCompanyDiscountPolicy(
                "user1", company.getProductionCompanyID(), bad);
        assertFalse(res.isSuccess());
    }

    @Test
    public void createCompanyDiscountPolicy_FailInvalidRecord_UnknownType() {
        DiscountPolicyRecord bad = new DiscountPolicyRecord(
                "BANANA", 10.0, null, null, null, null, null, null, null, null);
        Result<Boolean> res = discountPolicyService.createCompanyDiscountPolicy(
                "user1", company.getProductionCompanyID(), bad);
        assertFalse(res.isSuccess());
    }

    @Test
    public void createCompanyDiscountPolicy_FailComposite_NoChildren() {
        DiscountPolicyRecord bad = new DiscountPolicyRecord(
                "AND", 10.0, null, null, null, null, null, null, null, List.of());
        Result<Boolean> res = discountPolicyService.createCompanyDiscountPolicy(
                "user1", company.getProductionCompanyID(), bad);
        assertFalse(res.isSuccess());
    }

    // ================================================================
    // createEventDiscountPolicy
    // ================================================================

    @Test
    public void createEventDiscountPolicy_SimpleDiscount_Success() {
        Result<Boolean> res = discountPolicyService.createEventDiscountPolicy(
                "user1", e1.getEventID(), simple(10));
        assertTrue(res.isSuccess());
    }

    @Test
    public void createEventDiscountPolicy_FailInvalidToken() {
        Result<Boolean> res = discountPolicyService.createEventDiscountPolicy(
                "invalid_token", e1.getEventID(), simple(10));
        assertFalse(res.isSuccess());
    }

    @Test
    public void createEventDiscountPolicy_FailNoPermission() {
        Result<Boolean> res = discountPolicyService.createEventDiscountPolicy(
                "user2", e1.getEventID(), simple(10));
        assertFalse(res.isSuccess());
    }

    @Test
    public void createEventDiscountPolicy_FailEventNotFound() {
        Result<Boolean> res = discountPolicyService.createEventDiscountPolicy(
                "user1", 999, simple(10));
        assertFalse(res.isSuccess());
    }

    // ================================================================
    // editCompanyDiscountPolicy
    // ================================================================

    @Test
    public void editCompanyDiscountPolicy_Success() {
        // First create one, then replace it
        discountPolicyService.createCompanyDiscountPolicy(
                "user1", company.getProductionCompanyID(), simple(10));
        Result<Boolean> res = discountPolicyService.editCompanyDiscountPolicy(
                "user1", company.getProductionCompanyID(), simple(20));
        assertTrue(res.isSuccess());
    }

    @Test
    public void editCompanyDiscountPolicy_FailInvalidToken() {
        Result<Boolean> res = discountPolicyService.editCompanyDiscountPolicy(
                "invalid_token", company.getProductionCompanyID(), simple(20));
        assertFalse(res.isSuccess());
    }

    @Test
    public void editCompanyDiscountPolicy_FailNoPermission() {
        Result<Boolean> res = discountPolicyService.editCompanyDiscountPolicy(
                "user2", company.getProductionCompanyID(), simple(20));
        assertFalse(res.isSuccess());
    }

    @Test
    public void editCompanyDiscountPolicy_FailCompanyNotFound() {
        Result<Boolean> res = discountPolicyService.editCompanyDiscountPolicy(
                "user1", 999, simple(20));
        assertFalse(res.isSuccess());
    }

    // ================================================================
    // editEventDiscountPolicy
    // ================================================================

    @Test
    public void editEventDiscountPolicy_Success() {
        discountPolicyService.createEventDiscountPolicy(
                "user1", e1.getEventID(), simple(10));
        Result<Boolean> res = discountPolicyService.editEventDiscountPolicy(
                "user1", e1.getEventID(), simple(20));
        assertTrue(res.isSuccess());
    }

    @Test
    public void editEventDiscountPolicy_FailInvalidToken() {
        Result<Boolean> res = discountPolicyService.editEventDiscountPolicy(
                "invalid_token", e1.getEventID(), simple(20));
        assertFalse(res.isSuccess());
    }

    @Test
    public void editEventDiscountPolicy_FailNoPermission() {
        Result<Boolean> res = discountPolicyService.editEventDiscountPolicy(
                "user2", e1.getEventID(), simple(20));
        assertFalse(res.isSuccess());
    }

    @Test
    public void editEventDiscountPolicy_FailEventNotFound() {
        Result<Boolean> res = discountPolicyService.editEventDiscountPolicy(
                "user1", 999, simple(20));
        assertFalse(res.isSuccess());
    }

    // ================================================================
    // getEventDiscountPolicy
    // ================================================================

    @Test
    public void getEventDiscountPolicy_NoPolicyReturnsNull() {
        Result<DiscountPolicyDTO> res = discountPolicyService.getEventDiscountPolicy(
                "user1", e1.getEventID());
        assertTrue(res.isSuccess());
        // no policy set — returns ok(null)
    }

    @Test
    public void getEventDiscountPolicy_WithPolicy_Success() {
        discountPolicyService.createEventDiscountPolicy(
                "user1", e1.getEventID(), simple(10));
        Result<DiscountPolicyDTO> res = discountPolicyService.getEventDiscountPolicy(
                "user1", e1.getEventID());
        assertTrue(res.isSuccess());
    }

    @Test
    public void getEventDiscountPolicy_FailInvalidToken() {
        Result<DiscountPolicyDTO> res = discountPolicyService.getEventDiscountPolicy(
                "invalid_token", e1.getEventID());
        assertFalse(res.isSuccess());
    }

    @Test
    public void getEventDiscountPolicy_FailEventNotFound() {
        Result<DiscountPolicyDTO> res = discountPolicyService.getEventDiscountPolicy(
                "user1", 999);
        assertFalse(res.isSuccess());
    }

    // ================================================================
    // getCompanyDiscountPolicy
    // ================================================================

    @Test
    public void getCompanyDiscountPolicy_NoPolicyReturnsNull() {
        Result<DiscountPolicyDTO> res = discountPolicyService.getCompanyDiscountPolicy(
                "user1", company.getProductionCompanyID());
        assertTrue(res.isSuccess());
    }

    @Test
    public void getCompanyDiscountPolicy_WithPolicy_Success() {
        discountPolicyService.createCompanyDiscountPolicy(
                "user1", company.getProductionCompanyID(), simple(10));
        Result<DiscountPolicyDTO> res = discountPolicyService.getCompanyDiscountPolicy(
                "user1", company.getProductionCompanyID());
        assertTrue(res.isSuccess());
    }

    @Test
    public void getCompanyDiscountPolicy_FailInvalidToken() {
        Result<DiscountPolicyDTO> res = discountPolicyService.getCompanyDiscountPolicy(
                "invalid_token", company.getProductionCompanyID());
        assertFalse(res.isSuccess());
    }

    @Test
    public void getCompanyDiscountPolicy_FailCompanyNotFound() {
        Result<DiscountPolicyDTO> res = discountPolicyService.getCompanyDiscountPolicy(
                "user1", 999);
        assertFalse(res.isSuccess());
    }
}