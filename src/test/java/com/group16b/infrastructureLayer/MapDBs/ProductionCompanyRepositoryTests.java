package com.group16b.infrastructureLayer.MapDBs;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;

import com.group16b.InfrastructureLayer.MapDBs.ProductionCompanyRepositoryMapImpl;

import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;

public class ProductionCompanyRepositoryTests {
private ProductionCompanyRepositoryMapImpl repo;

    @BeforeEach
    void setUp() {
        repo = new ProductionCompanyRepositoryMapImpl();
    }

    private ProductionCompany createCompany(int id, String name) {
        ProductionCompany c = new ProductionCompany(id, name,1.1);
        c.setVersion(1);
        return c;
    }

    @Test
    void save_newCompany_success() {
        ProductionCompany company = createCompany(1, "Pixar");

        repo.save(company);

        ProductionCompany result = repo.findByID("1");

        assertEquals(1, result.getProductionCompanyID());
        assertEquals("Pixar", result.getName());
        assertEquals(1, result.getVersion());
    }

    @Test
    void findByID_invalidId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> repo.findByID("999"));
    }

    @Test
    void findByID_invalidFormat_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> repo.findByID("abc"));
    }

    @Test
    void getIDByName_success() {
        ProductionCompany company = createCompany(2, "Disney");

        repo.save(company);

        int id = repo.getIDByName("Disney");

        assertEquals(2, id);
    }

    @Test
    void getIDByName_notFound_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> repo.getIDByName("NonExistent"));
    }

    @Test
    void delete_companyRemoved() {
        ProductionCompany company = createCompany(3, "Marvel");

        repo.save(company);
        repo.delete("3");

        assertThrows(IllegalArgumentException.class,
                () -> repo.findByID("3"));
    }

    @Test
    void save_duplicateName_throws() {
        repo.save(createCompany(1, "Sony"));

        assertThrows(IllegalArgumentException.class,
                () -> repo.save(createCompany(2, "Sony")));
    }

    @Test
    void save_update_success_incrementsVersion() {
        ProductionCompany company = createCompany(10, "Netflix");

        repo.save(company);

        ProductionCompany updated = new ProductionCompany(company);
        updated.setName("NetflixUpdated");
        updated.setVersion(1);

        repo.save(updated);

        ProductionCompany result = repo.findByID("10");

        assertEquals("NetflixUpdated", result.getName());
        assertEquals(2, result.getVersion());
    }

    @Test
    void save_update_versionMismatch_throwsOptimisticLock() {
        ProductionCompany company = createCompany(11, "Paramount");

        repo.save(company);

        ProductionCompany stale = new ProductionCompany(company);
        stale.setVersion(999); // wrong version

        assertThrows(OptimisticLockingFailureException.class,
                () -> repo.save(stale));
    }

    @Test
    void rename_company_updatesNameIndex() {
        ProductionCompany company = createCompany(12, "OldName");

        repo.save(company);

        ProductionCompany updated = new ProductionCompany(company);
        updated.setName("NewName");
        updated.setVersion(1);

        repo.save(updated);

        assertThrows(IllegalArgumentException.class,
                () -> repo.getIDByName("OldName"));

        assertEquals(12, repo.getIDByName("NewName"));
    }

    @Test
    void getAll_returnsCopies_notOriginalReferences() {
        repo.save(createCompany(1, "A"));
        repo.save(createCompany(2, "B"));

        List<ProductionCompany> list = repo.getAl();

        assertEquals(2, list.size());

        // modify returned object should NOT affect repo
        list.get(0).setName("HACKED");

        ProductionCompany fresh = repo.findByID("1");
        assertNotEquals("HACKED", fresh.getName());
    }
    
}
