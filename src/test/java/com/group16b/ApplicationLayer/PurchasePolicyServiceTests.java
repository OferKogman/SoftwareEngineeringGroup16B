package com.group16b.ApplicationLayer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import com.group16b.ApplicationLayer.Records.PurchasePolicyRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Interfaces.ILocationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Policies.PurchasePolicy.LotteryPolicy;
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
import com.group16b.DomainLayer.VirtualQueue.VirtualQueue;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.ProductionCompanyRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.VenueRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.VirtualQueueRepositoryMapImpl;
import com.group16b.DomainLayer.Policies.PurchasePolicy.MinTicketsPolicy;

public class PurchasePolicyServiceTests {
    private PurchasePolicyService purchasePolicyService;
    private IAuthenticationService mockTokenService;
    private ILocationService mockLocationService;
    private IProductionCompanyRepository productionCompanyRepository;
    private IRepository<User> userRepository;
    private IRepository<Venue> venueRepository;
    private IEventRepository eventRepository;
    private IRepository<VirtualQueue> virtualQueueRepository;
    private User user;
    private User user2;
    private Event e1;
    private Location location1;
    private Segment segment1;
    private ProductionCompany company;

    private LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    public void setUp() {
        mockTokenService = mock(IAuthenticationService.class);
        mockLocationService = mock(ILocationService.class);
        productionCompanyRepository = new ProductionCompanyRepositoryMapImpl();
        virtualQueueRepository = new VirtualQueueRepositoryMapImpl();
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
        when(mockTokenService.extractRoleFromToken("user1")).thenReturn("Signed");
        when(mockTokenService.isUserToken("user1")).thenReturn(true);
        when(mockTokenService.extractSubjectFromToken("user1")).thenReturn(String.valueOf(user.getEmail()));

        user2 = new User("testuser2", "password");
        userRepository.save(user2);
        when(mockTokenService.validateToken("user2")).thenReturn(true);
        when(mockTokenService.extractRoleFromToken("user2")).thenReturn("Signed");
        when(mockTokenService.isUserToken("user2")).thenReturn(true);
        when(mockTokenService.extractSubjectFromToken("user2")).thenReturn(String.valueOf(user2.getEmail()));

        company = new ProductionCompany(1, "Pixar", 3.5, "testuser");
        productionCompanyRepository.save(company);

        location1 = new Location("location1", "1", "street", "city", "state", "country", 0.00, 0.00);

        segment1 = new FieldSeg("segment1", 50, new GridRectangle(1, 2, 3, 4));
        Map<String, Segment> segmentMap = new TreeMap<>();
        segmentMap.put("segment1", segment1);

        Venue venue1 = new Venue("Test Venue", location1, segmentMap, "testVenueID", new VenueGrid(6, 7),
                new ConcurrentHashMap<String, Stage>(), new ConcurrentHashMap<String, Entrance>());

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
    public void createLotteryPolicy_Success() {
        Result<Boolean> res = purchasePolicyService.createLotteryPolicy("user1", e1.getEventID(), 1, "First Lottery",
                50,
                now.plusDays(5));
        assertTrue(res.isSuccess());
        Event e = eventRepository.findByID(Integer.toString(e1.getEventID()));
        assertDoesNotThrow(() -> e.getLotteryPolicy());
    }

    @Test
    public void createLotteryPolicy_FailInvalidToken() {
        Result<Boolean> res = purchasePolicyService.createLotteryPolicy("invalid_token", e1.getEventID(), 1,
                "First Lottery", 50,
                now.plusDays(5));
        assertFalse(res.isSuccess());
        Event e = eventRepository.findByID(Integer.toString(e1.getEventID()));
        assertThrows(IllegalStateException.class, () -> e.getLotteryPolicy());
    }

    @Test
    public void createLotteryPolicy_FailNotUserToken() {
        Result<Boolean> res = purchasePolicyService.createLotteryPolicy("guest", e1.getEventID(), 1, "First Lottery",
                50,
                now.plusDays(5));
        assertFalse(res.isSuccess());
        Event e = eventRepository.findByID(Integer.toString(e1.getEventID()));
        assertThrows(IllegalStateException.class, () -> e.getLotteryPolicy());
    }

    @Test
    public void createLotteryPolicy_FailUserNotFound() {
        userRepository.delete("testuser");
        Result<Boolean> res = purchasePolicyService.createLotteryPolicy("user1", e1.getEventID(), 1, "First Lottery",
                50,
                now.plusDays(5));
        assertFalse(res.isSuccess());
        Event e = eventRepository.findByID(Integer.toString(e1.getEventID()));
        assertThrows(IllegalStateException.class, () -> e.getLotteryPolicy());
    }

    @Test
    public void createLotteryPolicy_FailCompanyNotFound() {
        productionCompanyRepository.delete("1");
        Result<Boolean> res = purchasePolicyService.createLotteryPolicy("user1", e1.getEventID(), 1, "First Lottery",
                50,
                now.plusDays(5));
        assertFalse(res.isSuccess());
        Event e = eventRepository.findByID(Integer.toString(e1.getEventID()));
        assertThrows(IllegalStateException.class, () -> e.getLotteryPolicy());
    }

    @Test
    public void createLotteryPolicy_FailUserDoesNotHavePerms() {
        Result<Boolean> res = purchasePolicyService.createLotteryPolicy("user2", e1.getEventID(), 1, "First Lottery",
                50,
                now.plusDays(5));
        assertFalse(res.isSuccess());
        Event e = eventRepository.findByID(Integer.toString(e1.getEventID()));
        assertThrows(IllegalStateException.class, () -> e.getLotteryPolicy());
    }

    @Test
    public void createLotteryPolicy_FailEventNotFound() {
        Result<Boolean> res = purchasePolicyService.createLotteryPolicy("user1", 999, 1, "First Lottery",
                50,
                now.plusDays(5));
        assertFalse(res.isSuccess());
        Event e = eventRepository.findByID(Integer.toString(e1.getEventID()));
        assertThrows(IllegalStateException.class, () -> e.getLotteryPolicy());
    }

    @Test
    public void createLotteryPolicy_FailInvalidDate() {
        Result<Boolean> res = purchasePolicyService.createLotteryPolicy("user1", e1.getEventID(), 1, "First Lottery",
                50,
                now.minusDays(5));
        assertFalse(res.isSuccess());
        Event e = eventRepository.findByID(Integer.toString(e1.getEventID()));
        assertThrows(IllegalStateException.class, () -> e.getLotteryPolicy());
    }

    @Test
    public void createLotteryPolicy_TwoThreadsBothSucceeds() throws InterruptedException {
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicReference<Result<Boolean>> result1 = new AtomicReference<>();
        AtomicReference<Result<Boolean>> result2 = new AtomicReference<>();

        Runnable createPolicyTask1 = () -> {
            try {
                readyLatch.countDown();
                startLatch.await();
                result1.set(purchasePolicyService.createLotteryPolicy("user1", e1.getEventID(), 1, "First Lottery", 50,
                        now.plusDays(5)));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Runnable createPolicyTask2 = () -> {
            try {
                readyLatch.countDown();
                startLatch.await();
                result2.set(purchasePolicyService.createLotteryPolicy("user1", e1.getEventID(), 2, "Second Lottery", 50,
                        now.plusDays(5)));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Thread thread1 = new Thread(createPolicyTask1);
        Thread thread2 = new Thread(createPolicyTask2);

        thread1.start();
        thread2.start();

        readyLatch.await();
        startLatch.countDown();

        thread1.join();
        thread2.join();

        int successCount = 0;
        if (result1.get() != null && result1.get().isSuccess()) {
            successCount++;
        }
        if (result2.get() != null && result2.get().isSuccess()) {
            successCount++;
        }

        assertTrue(successCount == 2);

        Event e = eventRepository.findByID(Integer.toString(e1.getEventID()));
        assertDoesNotThrow(() -> e.getLotteryPolicy());
    }

    @Test
    public void enrollLotteryPolicy_Success() {
        Event e = eventRepository.findByID(Integer.toString(e1.getEventID()));
        LotteryPolicy lotteryPolicy = new LotteryPolicy(0, "Lottery", 50, now.plusDays(5));
        e.setLotteryPolicy(lotteryPolicy);
        eventRepository.save(e);
        Result<Boolean> res = purchasePolicyService.enrollInLottery("user1", e1.getEventID());
        assertTrue(res.isSuccess());
        Event e2 = eventRepository.findByID(Integer.toString(e1.getEventID()));
        assertTrue(e2.getLotteryPolicy().getParticipants().contains("testuser"));
    }

    @Test
    public void enrollLotteryPolicy_FailInvalidToken() {
        Event e = eventRepository.findByID(Integer.toString(e1.getEventID()));
        LotteryPolicy lotteryPolicy = new LotteryPolicy(0, "Lottery", 50, now.plusDays(5));
        e.setLotteryPolicy(lotteryPolicy);
        eventRepository.save(e);
        Result<Boolean> res = purchasePolicyService.enrollInLottery("invalid_token", e1.getEventID());
        assertFalse(res.isSuccess());
        Event e2 = eventRepository.findByID(Integer.toString(e1.getEventID()));
        assertFalse(e2.getLotteryPolicy().getParticipants().contains("testuser"));
    }

    @Test
    public void enrollLotteryPolicy_FailNotUserToken() {
        Event e = eventRepository.findByID(Integer.toString(e1.getEventID()));
        LotteryPolicy lotteryPolicy = new LotteryPolicy(0, "Lottery", 50, now.plusDays(5));
        e.addEventPurchasePolicy(lotteryPolicy);
        eventRepository.save(e);
        Result<Boolean> res = purchasePolicyService.enrollInLottery("guest", e1.getEventID());
        assertFalse(res.isSuccess());
        Event e2 = eventRepository.findByID(Integer.toString(e1.getEventID()));
        assertFalse(e2.getLotteryPolicy().getParticipants().contains("testuser"));
    }

    @Test
    public void enrollLotteryPolicy_FailUserNotFound() {
        Event e = eventRepository.findByID(Integer.toString(e1.getEventID()));
        LotteryPolicy lotteryPolicy = new LotteryPolicy(0, "Lottery", 50, now.plusDays(5));
        e.addEventPurchasePolicy(lotteryPolicy);
        eventRepository.save(e);
        userRepository.delete("testuser");
        Result<Boolean> res = purchasePolicyService.enrollInLottery("user1", e1.getEventID());
        assertFalse(res.isSuccess());
        Event e2 = eventRepository.findByID(Integer.toString(e1.getEventID()));
        assertFalse(e2.getLotteryPolicy().getParticipants().contains("testuser"));
    }

    @Test
    public void enrollLotteryPolicy_FailEventNotFound() {
        Event e = eventRepository.findByID(Integer.toString(e1.getEventID()));
        LotteryPolicy lotteryPolicy = new LotteryPolicy(0, "Lottery", 50, now.plusDays(5));
        e.addEventPurchasePolicy(lotteryPolicy);
        eventRepository.save(e);
        Result<Boolean> res = purchasePolicyService.enrollInLottery("user1", 999);
        assertFalse(res.isSuccess());
        Event e2 = eventRepository.findByID(Integer.toString(e1.getEventID()));
        assertFalse(e2.getLotteryPolicy().getParticipants().contains("testuser"));
    }

    @Test
    public void enrollLotteryPolicy_FailInactiveEvent() {
        Event e = eventRepository.findByID(Integer.toString(e1.getEventID()));
        LotteryPolicy lotteryPolicy = new LotteryPolicy(0, "Lottery", 50, now.plusDays(5));
        e.addEventPurchasePolicy(lotteryPolicy);
        e.deactivateEvent();
        eventRepository.save(e);
        Result<Boolean> res = purchasePolicyService.enrollInLottery("user1", e1.getEventID());
        assertFalse(res.isSuccess());
        Event e2 = eventRepository.findByID(Integer.toString(e1.getEventID()));
        assertFalse(e2.getLotteryPolicy().getParticipants().contains("testuser"));
    }

    @Test
    public void enrollLotteryPolicy_FailPastEnrollDate() throws InterruptedException {
        Event e = eventRepository.findByID(Integer.toString(e1.getEventID()));
        LotteryPolicy lotteryPolicy = new LotteryPolicy(0, "Lottery", 50, now.plusSeconds(1));
        e.addEventPurchasePolicy(lotteryPolicy);
        eventRepository.save(e);
        Thread.sleep(1000);
        Result<Boolean> res = purchasePolicyService.enrollInLottery("user1", e1.getEventID());
        assertFalse(res.isSuccess());
        Event e2 = eventRepository.findByID(Integer.toString(e1.getEventID()));
        assertFalse(e2.getLotteryPolicy().getParticipants().contains("testuser"));
    }

    @Test
    public void enrollLotteryPolicy_FailEnrollTwice() {
        Event e = eventRepository.findByID(Integer.toString(e1.getEventID()));
        LotteryPolicy lotteryPolicy = new LotteryPolicy(0, "Lottery", 50, now.plusDays(5));
        e.addEventPurchasePolicy(lotteryPolicy);
        eventRepository.save(e);
        Result<Boolean> res = purchasePolicyService.enrollInLottery("user1", e1.getEventID());
        assertTrue(res.isSuccess());
        Event e2 = eventRepository.findByID(Integer.toString(e1.getEventID()));
        assertTrue(e2.getLotteryPolicy().getParticipants().contains("testuser"));
        Result<Boolean> res2 = purchasePolicyService.enrollInLottery("user1", e1.getEventID());
        assertFalse(res2.isSuccess());
        Event e3 = eventRepository.findByID(Integer.toString(e1.getEventID()));
        assertTrue(e3.getLotteryPolicy().getParticipants().contains("testuser"));
    }

    @Test
    public void enrollLotteryPolicy_TwoThreadsBothSucceeds() throws InterruptedException {
        Event eve = eventRepository.findByID(Integer.toString(e1.getEventID()));
        LotteryPolicy lotteryPolicy = new LotteryPolicy(0, "Lottery", 50, now.plusDays(5));
        eve.addEventPurchasePolicy(lotteryPolicy);
        eventRepository.save(eve);

        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicReference<Result<Boolean>> result1 = new AtomicReference<>();
        AtomicReference<Result<Boolean>> result2 = new AtomicReference<>();

        Runnable createPolicyTask1 = () -> {
            try {
                readyLatch.countDown();
                startLatch.await();
                result1.set(purchasePolicyService.enrollInLottery("user1", e1.getEventID()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Runnable createPolicyTask2 = () -> {
            try {
                readyLatch.countDown();
                startLatch.await();
                result2.set(purchasePolicyService.enrollInLottery("user2", e1.getEventID()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Thread thread1 = new Thread(createPolicyTask1);
        Thread thread2 = new Thread(createPolicyTask2);

        thread1.start();
        thread2.start();

        readyLatch.await();
        startLatch.countDown();

        thread1.join();
        thread2.join();

        assertTrue(result1.get().isSuccess(), result1.get().getError());
        assertTrue(result2.get().isSuccess(), result2.get().getError());

        Event e3 = eventRepository.findByID(Integer.toString(e1.getEventID()));
        assertTrue(e3.getLotteryPolicy().getParticipants().contains("testuser"));
        assertTrue(e3.getLotteryPolicy().getParticipants().contains("testuser2"));

        int successCount = 0;
        if (result1.get() != null && result1.get().isSuccess()) {
            successCount++;
        }
        if (result2.get() != null && result2.get().isSuccess()) {
            successCount++;
        }

        assertEquals(2, successCount);

        Event e = eventRepository.findByID(Integer.toString(e1.getEventID()));
        assertDoesNotThrow(() -> e.getLotteryPolicy());
    }

    @Test
    public void createLotteryPolicy_TwoThreadsOneSucceeds() throws InterruptedException {
        Event eve = eventRepository.findByID(Integer.toString(e1.getEventID()));
        LotteryPolicy lotteryPolicy = new LotteryPolicy(0, "Lottery", 50, now.plusDays(5));
        eve.addEventPurchasePolicy(lotteryPolicy);
        eventRepository.save(eve);

        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicReference<Result<Boolean>> result1 = new AtomicReference<>();
        AtomicReference<Result<Boolean>> result2 = new AtomicReference<>();

        Runnable createPolicyTask1 = () -> {
            try {
                readyLatch.countDown();
                startLatch.await();
                result1.set(purchasePolicyService.enrollInLottery("user1", e1.getEventID()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Runnable createPolicyTask2 = () -> {
            try {
                readyLatch.countDown();
                startLatch.await();
                result2.set(purchasePolicyService.enrollInLottery("user1", e1.getEventID()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Thread thread1 = new Thread(createPolicyTask1);
        Thread thread2 = new Thread(createPolicyTask2);

        thread1.start();
        thread2.start();

        readyLatch.await();
        startLatch.countDown();

        thread1.join();
        thread2.join();

        int successCount = 0;
        if (result1.get() != null && result1.get().isSuccess()) {
            successCount++;
        }
        if (result2.get() != null && result2.get().isSuccess()) {
            successCount++;
        }

        assertTrue(successCount == 1);

        Event e = eventRepository.findByID(Integer.toString(e1.getEventID()));
        assertDoesNotThrow(() -> e.getLotteryPolicy());
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
}
