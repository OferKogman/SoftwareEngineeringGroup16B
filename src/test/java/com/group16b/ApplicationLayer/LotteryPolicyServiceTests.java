/*
package com.group16b.ApplicationLayer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;

import com.group16b.ApplicationLayer.Interfaces.IAuthenticationService;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Policies.PurchasePolicy.LotteryPolicy;
import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.User.User;
import com.group16b.InfrastructureLayer.RequestContext;
import com.group16b.InfrastructureLayer.MapDBs.EventRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.ProductionCompanyRepositoryMapImpl;
import com.group16b.InfrastructureLayer.MapDBs.UserRepositoryMapImpl;
import com.group16b.InfrastructureLayer.Security.Role;

public class LotteryPolicyServiceTests {
    private LotteryPolicyService lotteryPolicyService;

    private IProductionCompanyRepository productionCompanyRepository;
    private IRepository<User> userRepository;
    private IEventRepository eventRepository;
    private IAuthenticationService authService;

    private final int BAD_EVENT_ID = 100;
    private final String FOUNDER_MAIL = "yuval hamebulbal";
    private final String PERMITED_MAIL = "the all mighty gargamel";
    private final String NOT_PERMITTED_MAIL = "the summer is cold";
    private final String BYSTANDER_MAIL = "toph?";
    private final String BAD_USER_MAIL = "who is me?";
    private final int COMPANY_ID = 1987;

    private Event event1;

    private final String LOTTERY_NAME = "some funny name idk";

    private LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setup() {
        userRepository = new UserRepositoryMapImpl();
        eventRepository = new EventRepositoryMapImpl();
        productionCompanyRepository = new ProductionCompanyRepositoryMapImpl();
        authService = mock(IAuthenticationService.class);
        when(authService.validateToken("user1")).thenReturn(true);
        when(authService.isUserToken("user1")).thenReturn(true);
        when(authService.extractSubjectFromToken("user1")).thenReturn(FOUNDER_MAIL);
        when(authService.validateToken("user2")).thenReturn(true);
        when(authService.isUserToken("user2")).thenReturn(false);
        when(authService.validateToken("user3")).thenReturn(true);
        when(authService.isUserToken("user3")).thenReturn(true);
        when(authService.extractSubjectFromToken("user3")).thenReturn(NOT_PERMITTED_MAIL);
        when(authService.validateToken("user4")).thenReturn(true);
        when(authService.isUserToken("user4")).thenReturn(true);
        when(authService.extractSubjectFromToken("user4")).thenReturn(BAD_USER_MAIL);
        when(authService.validateToken("user5")).thenReturn(true);
        when(authService.isUserToken("user5")).thenReturn(true);
        when(authService.extractSubjectFromToken("user5")).thenReturn(PERMITED_MAIL);
        when(authService.validateToken("user6")).thenReturn(true);
        when(authService.isUserToken("user6")).thenReturn(true);
        when(authService.extractSubjectFromToken("user6")).thenReturn(BYSTANDER_MAIL);

        lotteryPolicyService = new LotteryPolicyService(eventRepository, userRepository, productionCompanyRepository,
                authService);

        seedData();

    }

    private void seedData() {
        User founder = new User(FOUNDER_MAIL, FOUNDER_MAIL);
        User permited = new User(PERMITED_MAIL, PERMITED_MAIL);
        User notPermited = new User(NOT_PERMITTED_MAIL, NOT_PERMITTED_MAIL);
        User bystader = new User(BYSTANDER_MAIL, BYSTANDER_MAIL);

        userRepository.save(founder);
        userRepository.save(permited);
        userRepository.save(notPermited);
        userRepository.save(bystader);

        ProductionCompany company = new ProductionCompany(COMPANY_ID, "definetly not facebook", 6.7, FOUNDER_MAIL);
        assignManager(FOUNDER_MAIL, PERMITED_MAIL, Set.of(ManagerPermissions.PURCHASE_POLICY), company);
        assignManager(FOUNDER_MAIL, NOT_PERMITTED_MAIL, Set.of(ManagerPermissions.CUSTOMER_SUPPORT), company);

        productionCompanyRepository.save(company);

        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(2);

        event1 = new Event(new EventRecord("venven", "eve", startTime, endTime, "artist1", "category1", COMPANY_ID, 3.5),FOUNDER_MAIL);
        event1.activateEvent();
        eventRepository.save(event1);
    }

    private void assignManager(String founder, String manager, Set<ManagerPermissions> perms,
            ProductionCompany company) {
        company.AssignManager(founder, manager, perms);
        company.acceptInvite(manager, founder);
    }

    @Test
    public void createLotteryPolicy_Success() {
        RequestContext.set(PERMITED_MAIL, Role.SIGNED);
        Result<Void> res = lotteryPolicyService.createLotteryPolicy(event1.getEventID(), 1, LOTTERY_NAME, 50,
                now.plusDays(5), "user1");
        assertTrue(res.isSuccess());
        Event e = eventRepository.findByID(Integer.toString(event1.getEventID()));
        assertDoesNotThrow(() -> e.getLotteryPolicy());
        assertEquals(LOTTERY_NAME, e.getLotteryPolicy().getLotteryName());
    }

    @Test
    public void createLotteryPolicy_InvalidArgs_fail() {
        RequestContext.set(PERMITED_MAIL, Role.SIGNED);
        Result<Void> res = lotteryPolicyService.createLotteryPolicy(event1.getEventID(), 1, LOTTERY_NAME, 50,
                now.minusDays(5), "user1");
        assertFalse(res.isSuccess());
        assertEquals("Lottery registration due date cannot be in the past.", res.getError());
        Event e = eventRepository.findByID(Integer.toString(event1.getEventID()));
        assertThrows(IllegalStateException.class, () -> e.getLotteryPolicy());
    }

    @Test
    public void createLotteryPolicy_FailNotUserToken() {
        RequestContext.set(PERMITED_MAIL, Role.GUEST);
        Result<Void> res = lotteryPolicyService.createLotteryPolicy(event1.getEventID(), 1, LOTTERY_NAME, 50,
                now.plusDays(5), "user2");
        assertFalse(res.isSuccess());
        assertEquals("Only users are allowed to perform operation", res.getError());
        Event e = eventRepository.findByID(Integer.toString(event1.getEventID()));
        assertThrows(IllegalStateException.class, () -> e.getLotteryPolicy());
    }

    @Test
    public void createLotteryPolicy_FailCompanyNotFound() {
        RequestContext.set(PERMITED_MAIL, Role.SIGNED);
        productionCompanyRepository.delete(String.valueOf(COMPANY_ID));
        Result<Void> res = lotteryPolicyService.createLotteryPolicy(event1.getEventID(), 1, LOTTERY_NAME, 50,
                now.plusDays(5), "user1");
        assertFalse(res.isSuccess());
        assertEquals("Production company with ID " + COMPANY_ID + " is not found.", res.getError());
        Event e = eventRepository.findByID(Integer.toString(event1.getEventID()));
        assertThrows(IllegalStateException.class, () -> e.getLotteryPolicy());
    }

    @Test
    public void createLotteryPolicy_FailUserDoesNotHavePerms() {
        RequestContext.set(NOT_PERMITTED_MAIL, Role.SIGNED);
        Result<Void> res = lotteryPolicyService.createLotteryPolicy(event1.getEventID(), 1, LOTTERY_NAME, 50,
                now.plusDays(5), "user3");
        assertFalse(res.isSuccess());
        assertEquals("user " + NOT_PERMITTED_MAIL + " dont have correct permissions in company " + COMPANY_ID,
                res.getError());
        Event e = eventRepository.findByID(Integer.toString(event1.getEventID()));
        assertThrows(IllegalStateException.class, () -> e.getLotteryPolicy());
    }

    @Test
    public void createLotteryPolicy_FailEventNotFound() {
        RequestContext.set(NOT_PERMITTED_MAIL, Role.SIGNED);
        Result<Void> res = lotteryPolicyService.createLotteryPolicy(BAD_EVENT_ID, 1, LOTTERY_NAME, 50, now.plusDays(5),
                "user1");
        assertFalse(res.isSuccess());
        assertEquals("Event with ID " + BAD_EVENT_ID + " not found", res.getError());
        Event e = eventRepository.findByID(Integer.toString(event1.getEventID()));
        assertThrows(IllegalStateException.class, () -> e.getLotteryPolicy());
    }

    @Test
    public void createLotteryPolicy_FailInvalidDate() {
        RequestContext.set(PERMITED_MAIL, Role.SIGNED);
        Result<Void> res = lotteryPolicyService.createLotteryPolicy(event1.getEventID(), 1, LOTTERY_NAME, 50,
                now.minusDays(5), "user1");
        assertFalse(res.isSuccess());
        assertEquals("Lottery registration due date cannot be in the past.", res.getError());
        Event e = eventRepository.findByID(Integer.toString(event1.getEventID()));
        assertThrows(IllegalStateException.class, () -> e.getLotteryPolicy());
    }

    @Test
    void createLotteryPolicy_UserNotFound() {
        RequestContext.set(BAD_USER_MAIL, Role.SIGNED);

        Result<Void> res = lotteryPolicyService.createLotteryPolicy(event1.getEventID(), 1, LOTTERY_NAME, 50,
                now.plusDays(5), "user4");

        assertFalse(res.isSuccess());
        assertEquals("User with ID " + BAD_USER_MAIL + " not found.", res.getError());
    }

    @Test
    void createLotteryPolicy_EventAlreadyHasLotteryPolicy() {
        RequestContext.set(PERMITED_MAIL, Role.SIGNED);

        assertTrue(lotteryPolicyService
                .createLotteryPolicy(event1.getEventID(), 1, LOTTERY_NAME, 50, now.plusDays(5), "user1")
                .isSuccess());

        Result<Void> res = lotteryPolicyService.createLotteryPolicy(event1.getEventID(), 2, "another", 50,
                now.plusDays(5), "user1");

        assertFalse(res.isSuccess());
        assertEquals("Event already has a lottery policy.", res.getError());
    }

    @Test
    void createLotteryPolicy_unexpectedError() {
        RequestContext.set(PERMITED_MAIL, Role.SIGNED);
        IProductionCompanyRepository mockCompanyRepository = mock(IProductionCompanyRepository.class);
        doThrow(new RuntimeException("sam gaz al abamperim")).when(mockCompanyRepository).findByID(anyString());

        lotteryPolicyService = new LotteryPolicyService(eventRepository, userRepository, mockCompanyRepository,
                authService);

        Result<Void> res = lotteryPolicyService.createLotteryPolicy(event1.getEventID(), 2, "another", 50,
                now.plusDays(5), "user1");
        assertFalse(res.isSuccess());
        assertEquals("An unexpected error occurred: sam gaz al abamperim", res.getError());
    }

    @Test
    public void createLotteryPolicy_TwoThreadsBothSucceeds() throws InterruptedException {
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicReference<Result<Void>> result1 = new AtomicReference<>();
        AtomicReference<Result<Void>> result2 = new AtomicReference<>();

        Runnable createPolicyTask1 = () -> {
            try {
                readyLatch.countDown();
                startLatch.await();
                RequestContext.set(PERMITED_MAIL, Role.SIGNED);
                result1.set(lotteryPolicyService.createLotteryPolicy(event1.getEventID(), 1, LOTTERY_NAME, 50,
                        now.plusDays(5), "user1"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Runnable createPolicyTask2 = () -> {
            try {
                readyLatch.countDown();
                startLatch.await();
                RequestContext.set(PERMITED_MAIL, Role.SIGNED);
                result2.set(lotteryPolicyService.createLotteryPolicy(event1.getEventID(), 2, "Second Lottery", 50,
                        now.plusDays(5), "user1"));
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

        Event e = eventRepository.findByID(Integer.toString(event1.getEventID()));
        assertDoesNotThrow(() -> e.getLotteryPolicy());

        Result<Void> errorResult = result1.get().isSuccess() ? result2.get() : result1.get();
        assertEquals("Event already has a lottery policy.", errorResult.getError());
    }

    @Test
    void createLotteryPolicy_retryAfterOptimisticLockingFailure_succeeds() {
        // Arrange
        IProductionCompanyRepository companyRepo = mock(IProductionCompanyRepository.class);
        IRepository<User> userRepo = mock(IRepository.class);
        IEventRepository eventRepo = mock(IEventRepository.class);

        LotteryPolicyService service = new LotteryPolicyService(eventRepo, userRepo, companyRepo, authService);

        User user = mock(User.class);
        ProductionCompany company = mock(ProductionCompany.class);

        Event event = mock(Event.class);

        when(userRepo.findByID(PERMITED_MAIL))
                .thenReturn(user);

        when(eventRepo.findByID(String.valueOf(event1.getEventID())))
                .thenReturn(event);

        when(event.getEventProductionCompanyID())
                .thenReturn(COMPANY_ID);

        when(companyRepo.findByID(String.valueOf(COMPANY_ID)))
                .thenReturn(company);

        // first save fails, second succeeds
        doThrow(new OptimisticLockingFailureException("conflict"))
                .doNothing()
                .when(eventRepo)
                .save(any(Event.class));

        RequestContext.set(PERMITED_MAIL, Role.SIGNED);

        // Act
        Result<Void> result = service.createLotteryPolicy(
                event1.getEventID(),
                1,
                LOTTERY_NAME,
                50,
                now.plusDays(5), "user5");

        // Assert
        assertEquals(null, result.getError());

        verify(eventRepo, times(2)).save(any(Event.class));

        verify(eventRepo, atLeast(2)).findByID(String.valueOf(event1.getEventID()));

        verify(event, times(2)).setLotteryPolicy(any(LotteryPolicy.class));
    }

    @Test
    void enrollInLottery_Success() {
        RequestContext.set(PERMITED_MAIL, Role.SIGNED);
        lotteryPolicyService.createLotteryPolicy(event1.getEventID(), 1, LOTTERY_NAME, 50, now.plusDays(5), "user5");

        RequestContext.set(BYSTANDER_MAIL, Role.SIGNED);
        Result<Void> res = lotteryPolicyService.enrollInLottery(event1.getEventID(), "user6");
        assertTrue(res.isSuccess());

        event1 = eventRepository.findByID(String.valueOf(event1.getEventID()));
        assertTrue(event1.getLotteryPolicy().getParticipants().contains(BYSTANDER_MAIL));
    }

    @Test
    void enrollInLottery_FailNotUserToken() {
        RequestContext.set(BYSTANDER_MAIL, Role.GUEST);

        Result<Void> res = lotteryPolicyService.enrollInLottery(event1.getEventID(), "user2");

        assertFalse(res.isSuccess());
        assertEquals("Only users are allowed to perform operation", res.getError());
    }

    @Test
    void enrollInLottery_FailUserNotFound() {
        RequestContext.set(PERMITED_MAIL, Role.SIGNED);

        lotteryPolicyService.createLotteryPolicy(event1.getEventID(), 1, LOTTERY_NAME, 50, now.plusDays(5), "user1");
        RequestContext.set(BAD_USER_MAIL, Role.SIGNED);
        Result<Void> res = lotteryPolicyService.enrollInLottery(event1.getEventID(), "user4");

        assertFalse(res.isSuccess());
        assertEquals("User with ID " + BAD_USER_MAIL + " not found.", res.getError());

        event1 = eventRepository.findByID(String.valueOf(event1.getEventID()));

        assertFalse(event1.getLotteryPolicy().getParticipants().contains(BAD_USER_MAIL));
    }

    @Test
    void enrollInLottery_FailEventNotFound() {
        RequestContext.set(BYSTANDER_MAIL, Role.SIGNED);

        Result<Void> res = lotteryPolicyService.enrollInLottery(BAD_EVENT_ID, "user1");

        assertFalse(res.isSuccess());
        assertEquals("Event with ID " + BAD_EVENT_ID + " not found", res.getError());
    }

    @Test
    public void enrollLotteryPolicy_FailInactiveEvent() {
        RequestContext.set(BYSTANDER_MAIL, Role.SIGNED);
        Event e = eventRepository.findByID(Integer.toString(event1.getEventID()));
        LotteryPolicy lotteryPolicy = new LotteryPolicy(0, "Lottery", 50, now.plusDays(5));
        e.setLotteryPolicy(lotteryPolicy);
        e.deactivateEvent();
        eventRepository.save(e);
        Result<Void> res = lotteryPolicyService.enrollInLottery(event1.getEventID(), "user1");
        assertFalse(res.isSuccess());
        Event e2 = eventRepository.findByID(Integer.toString(event1.getEventID()));
        assertFalse(e2.getLotteryPolicy().getParticipants().contains("testuser"));
    }

    @Test
    public void enrollLotteryPolicy_FailPastEnrollDate() throws InterruptedException {
        Event e = eventRepository.findByID(Integer.toString(event1.getEventID()));
        LotteryPolicy lotteryPolicy = new LotteryPolicy(0, "Lottery", 50, now.plusSeconds(1));
        e.setLotteryPolicy(lotteryPolicy);
        eventRepository.save(e);
        Thread.sleep(1000);
        RequestContext.set(BYSTANDER_MAIL, Role.SIGNED);
        Result<Void> res = lotteryPolicyService.enrollInLottery(event1.getEventID(), "user1");
        assertFalse(res.isSuccess());
        Event e2 = eventRepository.findByID(Integer.toString(event1.getEventID()));
        assertFalse(e2.getLotteryPolicy().getParticipants().contains("testuser"));
    }

    @Test
    void enrollInLottery_FailAlreadyEnrolled() {
        RequestContext.set(PERMITED_MAIL, Role.SIGNED);
        lotteryPolicyService.createLotteryPolicy(event1.getEventID(), 1, LOTTERY_NAME, 50, now.plusDays(5), "user5");

        RequestContext.set(BYSTANDER_MAIL, Role.SIGNED);

        assertTrue(lotteryPolicyService.enrollInLottery(event1.getEventID(), "user6").isSuccess());

        Result<Void> res = lotteryPolicyService.enrollInLottery(event1.getEventID(), "user6");

        assertFalse(res.isSuccess());
        assertEquals("User is already enrolled in the lottery.", res.getError());

        event1 = eventRepository.findByID(String.valueOf(event1.getEventID()));

        assertEquals(1, event1.getLotteryPolicy().getParticipants().stream().filter(BYSTANDER_MAIL::equals).count());
    }

    @Test
    void enrollInLottery_unexpectedError() {
        RequestContext.set(PERMITED_MAIL, Role.SIGNED);
        IEventRepository mockEventRepository = mock(IEventRepository.class);
        doThrow(new RuntimeException("sam gaz al abamperim")).when(mockEventRepository).findByID(anyString());

        lotteryPolicyService = new LotteryPolicyService(mockEventRepository, userRepository,
                productionCompanyRepository, authService);

        Result<Void> res = lotteryPolicyService.enrollInLottery(event1.getEventID(), "user1");
        assertFalse(res.isSuccess());
        assertEquals("An unexpected error occurred: sam gaz al abamperim", res.getError());
    }

    @Test
    public void enrollLotteryPolicy_TwoThreadsBothSucceeds() throws InterruptedException {
        Event eve = eventRepository.findByID(Integer.toString(event1.getEventID()));
        LotteryPolicy lotteryPolicy = new LotteryPolicy(0, "Lottery", 50, now.plusDays(5));
        eve.setLotteryPolicy(lotteryPolicy);
        eventRepository.save(eve);

        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicReference<Result<Void>> result1 = new AtomicReference<>();
        AtomicReference<Result<Void>> result2 = new AtomicReference<>();

        Runnable createPolicyTask1 = () -> {
            try {
                RequestContext.set(PERMITED_MAIL, Role.SIGNED);
                readyLatch.countDown();
                startLatch.await();
                result1.set(lotteryPolicyService.enrollInLottery(event1.getEventID(), "user1"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Runnable createPolicyTask2 = () -> {
            try {
                readyLatch.countDown();
                startLatch.await();
                RequestContext.set(NOT_PERMITTED_MAIL, Role.SIGNED);
                result2.set(lotteryPolicyService.enrollInLottery(event1.getEventID(), "user6"));
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

        Event e3 = eventRepository.findByID(Integer.toString(event1.getEventID()));
        assertTrue(e3.getLotteryPolicy().getParticipants().contains(FOUNDER_MAIL));
        assertTrue(e3.getLotteryPolicy().getParticipants().contains(BYSTANDER_MAIL));

        int successCount = 0;
        if (result1.get() != null && result1.get().isSuccess()) {
            successCount++;
        }
        if (result2.get() != null && result2.get().isSuccess()) {
            successCount++;
        }

        assertEquals(2, successCount);

        Event e = eventRepository.findByID(Integer.toString(event1.getEventID()));
        assertDoesNotThrow(() -> e.getLotteryPolicy());
    }

    @Test
    public void createLotteryPolicy_TwoThreadsOneSucceeds() throws InterruptedException {
        Event eve = eventRepository.findByID(Integer.toString(event1.getEventID()));
        LotteryPolicy lotteryPolicy = new LotteryPolicy(0, "Lottery", 50, now.plusDays(5));
        eve.setLotteryPolicy(lotteryPolicy);
        eventRepository.save(eve);

        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicReference<Result<Void>> result1 = new AtomicReference<>();
        AtomicReference<Result<Void>> result2 = new AtomicReference<>();

        Runnable createPolicyTask1 = () -> {
            try {
                readyLatch.countDown();
                startLatch.await();
                RequestContext.set(PERMITED_MAIL, Role.SIGNED);
                result1.set(lotteryPolicyService.enrollInLottery(event1.getEventID(), "user1"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Runnable createPolicyTask2 = () -> {
            try {
                readyLatch.countDown();
                startLatch.await();
                RequestContext.set(PERMITED_MAIL, Role.SIGNED);
                result2.set(lotteryPolicyService.enrollInLottery(event1.getEventID(), "user1"));
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

        Event e = eventRepository.findByID(Integer.toString(event1.getEventID()));
        assertDoesNotThrow(() -> e.getLotteryPolicy());
    }

    @Test
    void enrollInLottery_retryAfterOptimisticLockingFailure_succeeds() {
        IRepository<User> userRepo = mock(IRepository.class);
        IEventRepository eventRepo = mock(IEventRepository.class);
        IProductionCompanyRepository companyRepo = mock(IProductionCompanyRepository.class);

        LotteryPolicyService service = new LotteryPolicyService(eventRepo, userRepo, companyRepo, authService);

        User user = mock(User.class);
        Event event = mock(Event.class);

        when(userRepo.findByID(BYSTANDER_MAIL))
                .thenReturn(user);

        when(user.getEmail())
                .thenReturn(BYSTANDER_MAIL);

        when(eventRepo.findByID(
                String.valueOf(event1.getEventID())))
                .thenReturn(event);

        doThrow(new OptimisticLockingFailureException("conflict"))
                .doNothing()
                .when(eventRepo)
                .save(any(Event.class));

        RequestContext.set(BYSTANDER_MAIL, Role.SIGNED);

        Result<Void> result = service.enrollInLottery(event1.getEventID(), "user6");

        assertEquals(null, result.getError());
        assertTrue(result.isSuccess());

        verify(eventRepo, times(2))
                .save(any(Event.class));

        verify(event, times(2))
                .enrollInLottery(BYSTANDER_MAIL);
    }

    @Test
    void handleLotteryResults_Success() {
        createLotteryAndEnroll();
        RequestContext.set(PERMITED_MAIL, Role.SIGNED);

        Result<Void> res = lotteryPolicyService.handleLotteryResults(event1.getEventID(), "user5");

        assertTrue(res.isSuccess());

        Event e = eventRepository.findByID(String.valueOf(event1.getEventID()));

        assertTrue(e.getLotteryPolicy().getWinners().contains(PERMITED_MAIL));
        assertEquals(1, e.getLotteryPolicy().getWinners().size());
    }

    @Test
    void handleLotteryResults_noLottery_cail() {
        RequestContext.set(PERMITED_MAIL, Role.SIGNED);

        Result<Void> res = lotteryPolicyService.handleLotteryResults(event1.getEventID(), "user1");

        assertFalse(res.isSuccess());
        assertEquals("Event does not have a lottery policy.", res.getError());
    }

    @Test
    void handleLotteryResults_FailNotUserToken() {
        createLotteryAndEnroll();
        RequestContext.set(PERMITED_MAIL, Role.GUEST);

        Result<Void> res = lotteryPolicyService.handleLotteryResults(event1.getEventID(), "user2");

        assertFalse(res.isSuccess());
        assertEquals("Only users are allowed to perform operation", res.getError());

        Event e = eventRepository.findByID(String.valueOf(event1.getEventID()));
        assertEquals(0, e.getLotteryPolicy().getWinners().size());
    }

    @Test
    void handleLotteryResults_FailUserNotFound() {
        createLotteryAndEnroll();

        RequestContext.set(BAD_USER_MAIL, Role.SIGNED);
        Result<Void> res = lotteryPolicyService.handleLotteryResults(event1.getEventID(), "user4");

        assertFalse(res.isSuccess());
        assertEquals("User with ID " + BAD_USER_MAIL + " not found.", res.getError());

        Event e = eventRepository.findByID(String.valueOf(event1.getEventID()));
        assertEquals(0, e.getLotteryPolicy().getWinners().size());
    }

    @Test
    void handleLotteryResults_FailEventNotFound() {
        RequestContext.set(PERMITED_MAIL, Role.SIGNED);

        Result<Void> res = lotteryPolicyService.handleLotteryResults(BAD_EVENT_ID, "user1");

        assertFalse(res.isSuccess());
        assertEquals("Event with ID " + BAD_EVENT_ID + " not found", res.getError());
    }

    @Test
    void handleLotteryResults_FailNoPermission() {
        createLotteryAndEnroll();

        RequestContext.set(NOT_PERMITTED_MAIL, Role.SIGNED);
        Result<Void> res = lotteryPolicyService.handleLotteryResults(event1.getEventID(), "user3");

        assertFalse(res.isSuccess());
        assertEquals("user " + NOT_PERMITTED_MAIL + " dont have correct permissions in company " + COMPANY_ID,
                res.getError());

        Event e = eventRepository.findByID(String.valueOf(event1.getEventID()));
        assertEquals(0, e.getLotteryPolicy().getWinners().size());
    }

    @Test
    void HandleLottery_unexpectedError() {
        RequestContext.set(PERMITED_MAIL, Role.SIGNED);
        IEventRepository mockEventRepository = mock(IEventRepository.class);
        doThrow(new RuntimeException("sam gaz al abamperim")).when(mockEventRepository).findByID(anyString());

        lotteryPolicyService = new LotteryPolicyService(mockEventRepository, userRepository,
                productionCompanyRepository, authService);

        Result<Void> res = lotteryPolicyService.handleLotteryResults(event1.getEventID(), "user1");
        assertFalse(res.isSuccess());
        assertEquals("An unexpected error occurred: sam gaz al abamperim", res.getError());
    }

    @Test
    void handleLotteryResults_retryAfterOptimisticLockingFailure_succeeds() {

        IProductionCompanyRepository companyRepo = mock(IProductionCompanyRepository.class);
        IRepository<User> userRepo = mock(IRepository.class);
        IEventRepository eventRepo = mock(IEventRepository.class);

        LotteryPolicyService service = new LotteryPolicyService(eventRepo, userRepo, companyRepo, authService);

        User user = mock(User.class);
        Event event = mock(Event.class);
        ProductionCompany company = mock(ProductionCompany.class);

        when(userRepo.findByID(PERMITED_MAIL))
                .thenReturn(user);

        when(user.getEmail())
                .thenReturn(PERMITED_MAIL);

        when(eventRepo.findByID(String.valueOf(event1.getEventID())))
                .thenReturn(event);

        when(event.getEventProductionCompanyID())
                .thenReturn(COMPANY_ID);

        when(companyRepo.findByID(String.valueOf(COMPANY_ID)))
                .thenReturn(company);

        doNothing().when(company)
                .validateUserPermissions(anyString(), any(ManagerPermissions.class));

        doThrow(new OptimisticLockingFailureException("conflict"))
                .doNothing()
                .when(eventRepo)
                .save(any(Event.class));

        RequestContext.set(PERMITED_MAIL, Role.SIGNED);

        Result<Void> res = service.handleLotteryResults(event1.getEventID(), "user5");

        assertTrue(res.isSuccess());

        verify(eventRepo, times(2)).save(any(Event.class));

        verify(event, times(2)).handleLotteryResults();
    }

    private void createLotteryAndEnroll() {
        RequestContext.set(PERMITED_MAIL, Role.SIGNED);
        lotteryPolicyService.createLotteryPolicy(event1.getEventID(), 1, LOTTERY_NAME, 50, now.plusDays(5), "user1");
        RequestContext.set(PERMITED_MAIL, Role.SIGNED);
        lotteryPolicyService.enrollInLottery(event1.getEventID(), "user5");

    }

}
*/