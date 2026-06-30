package com.group16b.infrastructureLayer.MapDBs;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.dao.OptimisticLockingFailureException;

import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.ProductionCompany.membership.ManagerPermissions;
import com.group16b.DomainLayer.User.User;
import com.group16b.InfrastructureLayer.MapDBs.ProductionCompanyRepositoryMapImpl;

public class ProductionCompanyRepositoryMapImplTests {

    private ProductionCompanyRepositoryMapImpl repo;

    private ProductionCompany pixar;
    private ProductionCompany disney;

    private final String OWNER_EMAIL = "owner";
    private final String MANAGER_EMAIL = "manager";

    private final int PIXAR_ID = 1;
    private final int DISNEY_ID = 2;
    private final String PIXAR_ID_STRING = String.valueOf(PIXAR_ID);
    private final String DISNEY_ID_STRING = String.valueOf(DISNEY_ID);

    @BeforeEach
    void setUp() {
        repo = new ProductionCompanyRepositoryMapImpl();

        pixar = createCompany(PIXAR_ID, "Pixar");
        disney = createCompany(DISNEY_ID, "Disney");
    }

    private ProductionCompany createCompany(int id, String name) {
        ProductionCompany company =
                new ProductionCompany(id, name, 1.1, OWNER_EMAIL);


        return company;
    }

    //save tests
    @Test
    void save_newCompany_success() {
        repo.save(pixar);
        ProductionCompany result = repo.findByID(PIXAR_ID_STRING);
        assertAll(
                () -> assertEquals(PIXAR_ID, result.getProductionCompanyID()),
                () -> assertEquals("Pixar", result.getName()),
                () -> assertEquals(1, result.getVersion())
        );
    }

    @Test
    void save_storesDefensiveCopy() {

        ProductionCompany original =spy(pixar);

        repo.save(original);

        original.setName("CHANGED");
        original.setVersion(999);

        ProductionCompany stored = repo.findByID(PIXAR_ID_STRING);

        assertAll(
                () -> assertEquals("Pixar", stored.getName()),
                () -> assertEquals(1, stored.getVersion()),
                () -> assertNotSame(original, stored)
        );
    }


    @Test
    void save_duplicateName_throwsIllegalArgumentException() {
        repo.save(pixar);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> repo.save(createCompany(99, "Pixar"))
        );

        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void save_update_success_incrementsVersion() {
        repo.save(pixar);
        ProductionCompany updated = repo.findByID(PIXAR_ID_STRING);
        updated.setName("PixarUpdated");
        repo.save(updated);
        ProductionCompany result = repo.findByID(PIXAR_ID_STRING);
        assertAll(
                () -> assertEquals("PixarUpdated", result.getName()),
                () -> assertEquals(2, result.getVersion())
        );
    }

    @Test
    void save_update_onlyStoredCopyChangesVersion() {
        repo.save(pixar);
        ProductionCompany detached =spy(repo.findByID(PIXAR_ID_STRING));
        long oldVersion = detached.getVersion();
        repo.save(detached);
        // detached object should NOT be mutated
        assertEquals(oldVersion, detached.getVersion());
        verify(detached, never()).setVersion(anyInt());
        ProductionCompany stored = repo.findByID(PIXAR_ID_STRING);
        assertEquals(oldVersion + 1, stored.getVersion());
    }

    @Test
    void save_update_returnsDefensiveCopy() {
        repo.save(pixar);
        ProductionCompany detached = repo.findByID(PIXAR_ID_STRING);
        repo.save(detached);
        ProductionCompany stored = repo.findByID(PIXAR_ID_STRING);
        assertNotSame(detached, stored);
    }

    @Test
    void save_update_versionMismatch_repositoryStateUnchanged() {
        repo.save(pixar);
        ProductionCompany stale = repo.findByID(PIXAR_ID_STRING);
        stale.setVersion(999);
        stale.setName("HACKED");
        assertThrows(
                OptimisticLockingFailureException.class,
                () -> repo.save(stale)
        );
        ProductionCompany actual = repo.findByID(PIXAR_ID_STRING);
        assertAll(
                () -> assertEquals("Pixar", actual.getName()),
                () -> assertEquals(1, actual.getVersion())
        );
    }

    @Test
    void save_failedUpdate_doesNotMutateCallerObject() {
        repo.save(pixar);
        ProductionCompany stale =spy(repo.findByID(PIXAR_ID_STRING));
        stale.setVersion(999);
        assertThrows(
                OptimisticLockingFailureException.class,
                () -> repo.save(stale)
        );
        verify(stale, never()).setVersion(1000);
        assertEquals(999, stale.getVersion());
    }

    @Test
    void save_rename_updatesNameIndex() {
        repo.save(pixar);
        ProductionCompany updated = repo.findByID(PIXAR_ID_STRING);
        updated.setName("NewPixar");
        repo.save(updated);
        assertThrows(
                IllegalArgumentException.class,
                () -> repo.getIDByName("Pixar")
        );
        assertEquals(PIXAR_ID, repo.getIDByName("NewPixar"));
    }

    @Test
    void save_renameFailure_repositoryStateUnchanged() {
        repo.save(pixar);
        repo.save(disney);
        ProductionCompany updated = repo.findByID(DISNEY_ID_STRING);
        updated.setName("Pixar");
        assertThrows(
                IllegalArgumentException.class,
                () -> repo.save(updated)
        );
        ProductionCompany actual = repo.findByID(DISNEY_ID_STRING);
        assertAll(
                () -> assertEquals("Disney", actual.getName()),
                () -> assertEquals(1, actual.getVersion())
        );
    }

    //find by id tests
    @Test
    void findByID_success() {
        repo.save(pixar);
        ProductionCompany result = repo.findByID(PIXAR_ID_STRING);
        assertEquals(PIXAR_ID, result.getProductionCompanyID());
    }

    @Test
    void findByID_invalidId_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> repo.findByID("999")
        );
    }

    @Test
    void findByID_invalidFormat_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> repo.findByID("abc")
        );
    }

    @Test
    void findByID_returnsDefensiveCopy() {
        repo.save(pixar);
        ProductionCompany found = repo.findByID(PIXAR_ID_STRING);
        found.setName("HACKED");
        ProductionCompany actual = repo.findByID(PIXAR_ID_STRING);
        assertEquals("Pixar", actual.getName());
    }

    //get by name tests

    @Test
    void getIDByName_success() {
        repo.save(disney);
        int id = repo.getIDByName("Disney");
        assertEquals(DISNEY_ID, id);
    }

    @Test
    void getIDByName_notFound_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> repo.getIDByName("Unknown")
        );
    }


    //delete tests
    @Test
    void delete_existingCompany_removedSuccessfully() {
        repo.save(pixar);
        repo.delete(PIXAR_ID_STRING);
        assertThrows(
                IllegalArgumentException.class,
                () -> repo.findByID(PIXAR_ID_STRING)
        );
    }

    @Test
    void delete_existingCompany_removesNameIndex() {
        repo.save(pixar);
        repo.delete(PIXAR_ID_STRING);
        assertThrows(
                IllegalArgumentException.class,
                () -> repo.getIDByName("Pixar")
        );
    }

    @Test
    void delete_invalidFormat_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> repo.delete("abc")
        );
    }

    @Test
    void delete_nonExistingCompany_ThrowsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> repo.delete("999")
        );
    }

    @Test
    void delete_oneCompany_keepsOtherCompaniesIntact() {
        repo.save(pixar);
        repo.save(disney);

        repo.delete(PIXAR_ID_STRING);

        assertAll(
                () -> assertEquals(DISNEY_ID, repo.getIDByName("Disney")),
                () -> assertEquals("Disney", repo.findByID(DISNEY_ID_STRING).getName()),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> repo.findByID(PIXAR_ID_STRING))
        );
    }

    //get all tests
    @Test
    void getAll_returnsAllCompanies() {
        repo.save(pixar);
        repo.save(disney);
        List<ProductionCompany> companies = repo.getAll();
        assertEquals(2, companies.size());
    }

    @Test
    void getAll_returnsCopies_notOriginalReferences() {
        repo.save(pixar);
        repo.save(disney);
        List<ProductionCompany> companies = repo.getAll();
        companies.get(0).setName("HACKED");
        ProductionCompany fresh = repo.findByID(PIXAR_ID_STRING);
        assertNotEquals("HACKED", fresh.getName());
    }

    //get all for user tests
    @Test
    void getAllUserCompanies_noCompanies_returnsEmptyList() {
        User user = mock(User.class);
        when(user.getEmail()).thenReturn(MANAGER_EMAIL);
        assertTrue(repo.getAllUserComapnies(user).isEmpty());
    }

    @Test
    void getAllUserCompanies_userManagesOneCompany_returnsCorrectID() {
        User user = mock(User.class);

        when(user.getEmail()).thenReturn(MANAGER_EMAIL);

        pixar.AssignManager(OWNER_EMAIL, MANAGER_EMAIL,Set.of( ManagerPermissions.PURCHASE_POLICY));
        pixar.acceptInvite(MANAGER_EMAIL, OWNER_EMAIL);

        repo.save(pixar);

        List<Integer> result =repo.getAllUserComapnies(user);

        assertEquals(List.of(PIXAR_ID), result);
    }

    @Test
    void getAllUserCompanies_userManagesMultipleCompanies_returnsCorrectIDs() {
        User user = mock(User.class);

        when(user.getEmail()).thenReturn(MANAGER_EMAIL);

        pixar.AssignManager(OWNER_EMAIL, MANAGER_EMAIL,Set.of( ManagerPermissions.PURCHASE_POLICY));
        pixar.acceptInvite(MANAGER_EMAIL, OWNER_EMAIL);

        disney.AssignManager(OWNER_EMAIL, MANAGER_EMAIL,Set.of( ManagerPermissions.PURCHASE_POLICY));
        disney.acceptInvite(MANAGER_EMAIL, OWNER_EMAIL);

        repo.save(pixar);
        repo.save(disney);

        List<Integer> result =repo.getAllUserComapnies(user);

        assertEquals(List.of(PIXAR_ID, DISNEY_ID), result);
    }

    //concurrency tests
    @Test
    void concurrentUpdates_onlyOneSucceeds() throws Exception {
        repo.save(pixar);
        ProductionCompany copy1 = repo.findByID(PIXAR_ID_STRING);
        ProductionCompany copy2 = repo.findByID(PIXAR_ID_STRING);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        ExecutorService executor =Executors.newFixedThreadPool(2);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        Runnable task1 = () -> {
            try {
                startLatch.await();

                copy1.setName("Update1");

                repo.save(copy1);

                successCount.incrementAndGet();

            } catch (OptimisticLockingFailureException e) {

                failureCount.incrementAndGet();

            } catch (Exception ignored) {
            }
        };

        Runnable task2 = () -> {
            try {
                startLatch.await();

                copy2.setName("Update2");

                repo.save(copy2);

                successCount.incrementAndGet();

            } catch (OptimisticLockingFailureException e) {

                failureCount.incrementAndGet();

            } catch (Exception ignored) {
            }
        };

        executor.submit(task1);
        executor.submit(task2);

        startLatch.countDown();

        executor.shutdown();

        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertEquals(1, successCount.get());
        assertEquals(1, failureCount.get());

        ProductionCompany finalState =
                repo.findByID(PIXAR_ID_STRING);

        assertEquals(2, finalState.getVersion());
    }

    @Test
    void concurrentInsert_sameName_onlyOneSucceeds() throws Exception {

        ProductionCompany company1 =
                createCompany(PIXAR_ID, "Sony");

        ProductionCompany company2 =
                createCompany(DISNEY_ID, "Sony");

        AtomicInteger successCount =
                new AtomicInteger();

        AtomicInteger failureCount =
                new AtomicInteger();

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        Runnable task1 = () -> {
            try {
                startLatch.await();
                repo.save(company1);
                successCount.incrementAndGet();

            } catch (IllegalArgumentException e) {
                failureCount.incrementAndGet();
            } catch (Exception ignored) {
            }
        };

        Runnable task2 = () -> {
            try {
                startLatch.await();
                repo.save(company2);
                successCount.incrementAndGet();

            } catch (IllegalArgumentException e) {
                failureCount.incrementAndGet();
            } catch (Exception ignored) {
            }
        };

        executor.submit(task1);
        executor.submit(task2);

        startLatch.countDown();

        executor.shutdown();

        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertEquals(1, successCount.get());
        assertEquals(1, failureCount.get());

        assertEquals(1, repo.getAll().size());
    }
}
