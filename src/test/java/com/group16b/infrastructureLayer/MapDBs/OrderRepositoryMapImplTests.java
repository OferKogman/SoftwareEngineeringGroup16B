package com.group16b.infrastructureLayer.MapDBs;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;

import com.group16b.DomainLayer.Order.Order;
import com.group16b.InfrastructureLayer.MapDBs.OrderRepositoryMapImpl;


class OrderRepositoryMapImplTests {

    private OrderRepositoryMapImpl repo;

    private Order seatOrder;
    private Order fieldOrder;
    private Order completedOrder;

    @BeforeEach
    void setUp() {
        repo = new OrderRepositoryMapImpl();

        seatOrder = new Order("segment1", List.of("A1", "A2"), 100.0, 1, "user1");
        fieldOrder = new Order("field1", 3, 150.0, 1, "user1");

        completedOrder = new Order("segment2", List.of("B1"), 50.0, 2, "user1");
        completedOrder.CompleteOrder();
    }

    @Test
    void save_newOrder_savesSuccessfully() {
        repo.save(seatOrder);

        Order result = repo.findByID(seatOrder.getOrderId());

        assertEquals(seatOrder.getOrderId(), result.getOrderId());
        assertEquals(0, result.getVersion());
    }

    @Test
    void save_existingOrderWithCorrectVersion_updatesAndIncrementsVersion() {
        repo.save(seatOrder);

        Order copy = repo.findByID(seatOrder.getOrderId());
        copy.updateSeats(List.of("A3", "A4"), 120.0);

        repo.save(copy);

        Order result = repo.findByID(seatOrder.getOrderId());

        assertEquals(seatOrder.getOrderId(), result.getOrderId());
        assertEquals(1, result.getVersion());
        assertEquals(List.of("A3", "A4"), result.getSeats());
    }

    @Test
    void save_existingOrderWithWrongVersion_throwsOptimisticLockingFailureExceptionAndDoesNotChangeOrder() {
        repo.save(seatOrder);

        Order staleCopy = repo.findByID(seatOrder.getOrderId());
        Order validCopy = repo.findByID(seatOrder.getOrderId());

        repo.save(validCopy);

        assertThrows(
                OptimisticLockingFailureException.class,
                () -> repo.save(staleCopy)
        );

        Order result = repo.findByID(seatOrder.getOrderId());

        assertEquals(1, result.getVersion());
    }

    @Test
    void findByID_existingOrder_returnsCopyWithSameId() {
        repo.save(seatOrder);

        Order result = repo.findByID(seatOrder.getOrderId());

        assertEquals(seatOrder.getOrderId(), result.getOrderId());
        assertNotSame(seatOrder, result);
    }

    @Test
    void findByID_missingOrder_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> repo.findByID("missing")
        );
    }

    @Test
    void getAll_returnsAllOrdersAsCopies() {
        repo.save(seatOrder);
        repo.save(fieldOrder);

        List<Order> result = repo.getAll();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(order -> order.getOrderId().equals(seatOrder.getOrderId())));
        assertTrue(result.stream().anyMatch(order -> order.getOrderId().equals(fieldOrder.getOrderId())));
    }

    @Test
    void getBySubjectId_returnsOnlyCompletedOrdersForSubject() {
        repo.save(seatOrder);
        repo.save(completedOrder);

        List<Order> result = repo.getBySubjectId("user1");

        assertEquals(1, result.size());
        assertEquals(completedOrder.getOrderId(), result.get(0).getOrderId());
    }

    @Test
    void delete_existingActiveOrder_deletesSuccessfully() {
        repo.save(seatOrder);

        repo.delete(seatOrder.getOrderId());

        assertThrows(
                IllegalArgumentException.class,
                () -> repo.findByID(seatOrder.getOrderId())
        );
    }

    @Test
    void delete_existingCompletedOrder_throwsUnsupportedOperationExceptionAndDoesNotDelete() {
        repo.save(completedOrder);

        assertThrows(
                UnsupportedOperationException.class,
                () -> repo.delete(completedOrder.getOrderId())
        );

        Order result = repo.findByID(completedOrder.getOrderId());

        assertEquals(completedOrder.getOrderId(), result.getOrderId());
    }

    @Test
    void delete_missingOrder_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> repo.delete("missing")
        );
    }

    @Test
    void save_concurrentStaleUpdates_onlyOneSucceedsAndOneFailsWithOptimisticLockingFailureException()
            throws InterruptedException {

        repo.save(seatOrder);

        Order copyA = repo.findByID(seatOrder.getOrderId());
        Order copyB = repo.findByID(seatOrder.getOrderId());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successes = new AtomicInteger(0);
        AtomicInteger failures = new AtomicInteger(0);

        Runnable taskA = () -> saveAfterLatch(copyA, startLatch, successes, failures);
        Runnable taskB = () -> saveAfterLatch(copyB, startLatch, successes, failures);

        executor.submit(taskA);
        executor.submit(taskB);

        startLatch.countDown();

        executor.shutdown();
        assertTrue(executor.awaitTermination(3, TimeUnit.SECONDS));

        assertEquals(1, successes.get());
        assertEquals(1, failures.get());

        Order result = repo.findByID(seatOrder.getOrderId());

        assertEquals(1, result.getVersion());
    }

    private void saveAfterLatch(
            Order order,
            CountDownLatch startLatch,
            AtomicInteger successes,
            AtomicInteger failures
    ) {
        try {
            startLatch.await();
            repo.save(order);
            successes.incrementAndGet();
        } catch (OptimisticLockingFailureException e) {
            failures.incrementAndGet();
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass().getSimpleName());
        }
    }
}

    