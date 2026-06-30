package com.group16b.infrastructureLayer.MapDBs;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group16b.DomainLayer.VirtualQueue.VirtualQueue;
import com.group16b.InfrastructureLayer.MapDBs.VirtualQueueRepositoryMapImpl;

public class VirtualQueueRepositoryMapImplTests {

    private VirtualQueueRepositoryMapImpl repo;

    private VirtualQueue vq1;
    private VirtualQueue vq2;

    private final String USER1 = "u1";
    private final String USER2 = "u2";

    private final int VQ1_ID = 1;
    private final int VQ2_ID = 2;
    private final String VQ1_ID_STRING = String.valueOf(VQ1_ID);
    private final String VQ2_ID_STRING = String.valueOf(VQ2_ID);

    @BeforeEach
    void setUp() {
        repo = new VirtualQueueRepositoryMapImpl();

        vq1 = spy(new VirtualQueue(VQ1_ID));
        vq2 = spy(new VirtualQueue(VQ2_ID));

        repo.save(vq1);
    }

    // save tests
    @Test
    void save_newCompany_success() {
        repo.save(vq2);
        VirtualQueue result = repo.findByID(VQ2_ID_STRING);
        assertEquals(VQ2_ID, result.getId());
    }

    @Test
    void save_storesDefensiveCopy() {
        vq1.addToQueue(USER1);
        VirtualQueue stored = repo.findByID(VQ1_ID_STRING);
        assertAll(
                () -> assertNotEquals(vq1.isUserPassedQueue(USER1), stored.isUserPassedQueue(USER1)),
                () -> assertNotSame(vq1, stored));
    }

    @Test
    void save_update_success() {
        VirtualQueue stored = repo.findByID(VQ1_ID_STRING);
        stored.addToQueue(USER1);
        repo.save(stored);
        VirtualQueue result = repo.findByID(VQ1_ID_STRING);
        assertEquals(stored.isUserPassedQueue(USER1), result.isUserPassedQueue(USER1));
    }

    @Test
    void save_update_savingExistingIdWithoutAccessingTheSavedObject() {
        vq1.addToQueue(USER1);
        assertThrows(
                IllegalStateException.class,
                () -> repo.save(vq1));
        VirtualQueue result = repo.findByID(VQ1_ID_STRING);
        assertNotEquals(vq1.isUserPassedQueue(USER1), result.isUserPassedQueue(USER1));
    }

    @Test
    void save_update_returnsDefensiveCopy() {
        VirtualQueue stored = repo.findByID(VQ1_ID_STRING);
        repo.save(stored);
        VirtualQueue result = repo.findByID(VQ1_ID_STRING);
        assertNotSame(stored, result);
    }

    // find by id tests

    @Test
    void findByID_success() {
        VirtualQueue result = repo.findByID(VQ1_ID_STRING);
        assertEquals(VQ1_ID, result.getId());
    }

    @Test
    void findByID_invalidId_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> repo.findByID("999"));
    }

    @Test
    void findByID_invalidFormat_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> repo.findByID("abc"));
    }

    // delete tests

    @Test
    void delete_existingCompany_removedSuccessfully() {
        repo.delete(VQ1_ID_STRING);
        assertThrows(
                IllegalArgumentException.class,
                () -> repo.findByID(VQ1_ID_STRING));
    }

    @Test
    void delete_invalidFormat_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> repo.delete("abc"));
    }

    @Test
    void delete_nonExistingCompany_doesNothing() {
        VirtualQueue before = repo.findByID(VQ1_ID_STRING);
        repo.delete("999");
        VirtualQueue after = repo.findByID(VQ1_ID_STRING);
        assertAll(
                () -> assertEquals(before.getId(), after.getId()),
                () -> assertEquals(before.isUserPassedQueue(USER1), after.isUserPassedQueue(USER1)),
                () -> assertNotSame(before, after)

        );

    }

    @Test
    void delete_oneCompany_keepsOtherCompaniesIntact() {
        repo.save(vq2);
        repo.delete(VQ2_ID_STRING);
        assertAll(
                () -> assertEquals(VQ1_ID, repo.findByID(VQ1_ID_STRING).getId()),
                () -> assertEquals(vq1.isUserPassedQueue(USER1), repo.findByID(VQ1_ID_STRING).isUserPassedQueue(USER1)),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> repo.findByID(VQ2_ID_STRING)));
    }

    // concurrency tests

    // @Test
    // void concurrentUpdates_onlyOneSucceeds() throws Exception {
    //     ExecutorService executor = Executors.newFixedThreadPool(2);
    //     CountDownLatch startLatch = new CountDownLatch(1);

    //     Runnable task1 = () -> {
    //         try {
    //             startLatch.await();
    //             VirtualQueue q = repo.findByID(VQ1_ID_STRING);
    //             q.addToQueue(USER1);
    //             Thread.sleep(1000);
    //             repo.save(q);
    //         } catch (Exception e) {
    //         }
    //     };

    //     Runnable task2 = () -> {
    //         try {
    //             startLatch.await();
    //             VirtualQueue q = repo.findByID(VQ1_ID_STRING);
    //             q.addToQueue(USER2);
    //             Thread.sleep(1000);
    //             repo.save(q);
    //         } catch (Exception e) {
    //         }
    //     };

    //     executor.submit(task1);
    //     executor.submit(task2);
    //     startLatch.countDown();
    //     executor.shutdown();
    //     executor.awaitTermination(10, TimeUnit.SECONDS);

    //     VirtualQueue result = repo.findByID(VQ1_ID_STRING);
    //     repo.save(result);

    //     boolean user1Passed = result.isUserPassedQueue(USER1);
    //     boolean user2Passed = result.isUserPassedQueue(USER2);

    //     assertTrue(user1Passed || user2Passed, "At least one user should have passed the queue");
    //     assertTrue(!(user1Passed && user2Passed), "Both users should not have passed the queue simultaneously");
    // }
}