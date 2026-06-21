package com.group16b.ApplicationLayer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import com.group16b.ApplicationLayer.Records.PurchasePolicyRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
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
import com.group16b.DomainLayer.Policies.PurchasePolicy.MinTicketsPolicy;

public class PurchasePolicyServiceTests {
    private PurchasePolicyService purchasePolicyService;
    private IAuthenticationService mockTokenService;
    private IProductionCompanyRepository productionCompanyRepository;
    private IRepository<User> userRepository;
    private IRepository<Venue> venueRepository;
    private IEventRepository eventRepository;
    private User user;
    private User user2;
    private Event e1;
    private Location location1;
    private Segment segment1;
    private ProductionCompany company;

    @BeforeEach
    public void setUp() {
        mockTokenService = mock(IAuthenticationService.class);
        productionCompanyRepository = new ProductionCompanyRepositoryMapImpl();
        eventRepository = new EventRepositoryMapImpl();
        venueRepository = new VenueRepositoryMapImpl();
        userRepository = new UserRepositoryMapImpl();
        purchasePolicyService = new PurchasePolicyService(mockTokenService, productionCompanyRepository,
                eventRepository, userRepository);

        when(mockTokenService.validateToken("invalid_token")).thenReturn(false);
        when(mockTokenService.validateToken("guest")).thenReturn(true);
        when(mockTokenService.isUserToken("guest")).thenReturn(false);

        user = new User("testuser", "password");
        userRepository.save(user);
        when(mockTokenService.validateToken("user1")).thenReturn(true);
        when(mockTokenService.extractRoleFromToken("user1")).thenReturn(Role.SIGNED);
        when(mockTokenService.isUserToken("user1")).thenReturn(true);
        when(mockTokenService.extractSubjectFromToken("user1")).thenReturn(String.valueOf(user.getEmail()));

        user2 = new User("testuser2", "password");
        userRepository.save(user2);
        when(mockTokenService.validateToken("user2")).thenReturn(true);
        when(mockTokenService.extractRoleFromToken("user2")).thenReturn(Role.SIGNED);
        when(mockTokenService.isUserToken("user2")).thenReturn(true);
        when(mockTokenService.extractSubjectFromToken("user2")).thenReturn(String.valueOf(user2.getEmail()));

        company = new ProductionCompany(1, "Pixar", 3.5, "testuser");
        productionCompanyRepository.save(company);

        location1 = new Location("location1", "1", "street", "city", "state", "country", 0.00, 0.00);

        segment1 = new FieldSeg("segment1", 50, new GridRectangle(1, 2, 3, 4));
        Map<String, Segment> segmentMap = new TreeMap<>();
        segmentMap.put("segment1", segment1);

        Venue venue1 = new Venue("Test Venue", location1, segmentMap, "testVenueID", new VenueGrid(6, 7),
                new ConcurrentHashMap<String, Stage>(), new ConcurrentHashMap<String, Entrance>(),1);

        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(2);

        e1 = new Event(new EventRecord("venue1", "event1", startTime, endTime, "artist1", "category1", 1, 5.0, 3.5),
                user.getEmail());
        e1.activateEvent();
        eventRepository.save(e1);
        venue1.bookEvent(e1.getEventStartTime(), e1.getEventEndTime(), 1);
        venueRepository.save(venue1);
    }

    @Test
    public void createEventPurchasePolicy_Success() {
        Result<Boolean> res = purchasePolicyService.createEventPurchasePolicy("user1", e1.getEventID(), new PurchasePolicyRecord("MIN_TICKETS", null, null, 1, null));
        assertTrue(res.isSuccess());
    }

    @Test
    public void createEventPurchasePolicy_FailInvalidToken() {
        Result<Boolean> res = purchasePolicyService.createEventPurchasePolicy("invalid_token", e1.getEventID(), new PurchasePolicyRecord("MIN_TICKETS", null, null, 1, null));
        assertFalse(res.isSuccess());
    }

    @Test
    public void createEventPurchasePolicy_FailNoPermission() {
        Result<Boolean> res = purchasePolicyService.createEventPurchasePolicy("user2", e1.getEventID(), new PurchasePolicyRecord("MIN_TICKETS", null, null, 1, null));
        assertFalse(res.isSuccess());
    }

    @Test
    public void createEventPurchasePolicy_FailEventNotFound() {
        Result<Boolean> res = purchasePolicyService.createEventPurchasePolicy("user1", 999, new PurchasePolicyRecord("MIN_TICKETS", null, null, 1, null));
        assertFalse(res.isSuccess());
    }

    @Test
    public void editCompanyPurchasePolicy_Success() {
        MinTicketsPolicy oldPolicy = new MinTicketsPolicy(1);
        company.addPurchasePolicy(oldPolicy);
        productionCompanyRepository.save(company);
        Result<Boolean> res = purchasePolicyService.editCompanyPurchasePolicy("user1", company.getProductionCompanyID(), new PurchasePolicyRecord("MIN_TICKETS", null, null, 2, null));
        assertTrue(res.isSuccess());
    }

    @Test
    public void editCompanyPurchasePolicy_FailInvalidToken() {
        MinTicketsPolicy oldPolicy = new MinTicketsPolicy(1);
        Result<Boolean> res = purchasePolicyService.editCompanyPurchasePolicy("invalid_token", company.getProductionCompanyID(), new PurchasePolicyRecord("MIN_TICKETS", null, null, 2, null));
        assertFalse(res.isSuccess());
    }

    @Test
    public void editCompanyPurchasePolicy_FailNoPermission() {
        MinTicketsPolicy oldPolicy = new MinTicketsPolicy(1);
        company.addPurchasePolicy(oldPolicy);
        productionCompanyRepository.save(company);
        Result<Boolean> res = purchasePolicyService.editCompanyPurchasePolicy("user2", company.getProductionCompanyID(), new PurchasePolicyRecord("MIN_TICKETS", null, null, 2, null));
        assertFalse(res.isSuccess());
    }

    @Test
    public void editCompanyPurchasePolicy_FailCompanyNotFound() {
        MinTicketsPolicy oldPolicy = new MinTicketsPolicy(1);
        Result<Boolean> res = purchasePolicyService.editCompanyPurchasePolicy("user1", 999, new PurchasePolicyRecord("MIN_TICKETS", null, null, 2, null));
        assertFalse(res.isSuccess());
    }
}
