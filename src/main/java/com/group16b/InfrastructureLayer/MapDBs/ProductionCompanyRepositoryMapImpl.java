package com.group16b.InfrastructureLayer.MapDBs;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.dao.OptimisticLockingFailureException;

import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;

public class ProductionCompanyRepositoryMapImpl implements IProductionCompanyRepository {

    // source of truth
    private final ConcurrentHashMap<Integer, ProductionCompany> companies =new ConcurrentHashMap<>();
    // secondary index
    private final ConcurrentHashMap<String, Integer> names =new ConcurrentHashMap<>();
    // protects consistency between maps
    private final Object lock = new Object();
    public ProductionCompanyRepositoryMapImpl() {
    }

    @Override
    public ProductionCompany findByID(String ID) {
        int id=parseID(ID);
        ProductionCompany company =companies.get(id);
        if(company == null) {
            throw new IllegalArgumentException("Production company with ID " + ID + " is not found.");
        }
        return new ProductionCompany(company);
    }

    @Override
    public int getIDByName(String name) {
        Integer id = names.get(name);
        if(id == null) {
             throw new IllegalArgumentException("Production company with name " + name + " is not found.");
        }
        return id;
    }

    @Override
    public List<ProductionCompany> getAll() {
        return companies.values()
                .stream()
                .map(ProductionCompany::new)
                .toList();
    }

    @Override
    public void delete(String ID) {
        int id=parseID(ID);

        synchronized (lock) {
            ProductionCompany removed = companies.remove(id);
            if(removed != null) {
                names.remove(removed.getName());
            }
        }
    }

    @Override
    public void save(ProductionCompany company) {
        synchronized (lock) {
            int id = company.getProductionCompanyID();
            ProductionCompany current = companies.get(id);

            // INSERT
            if(current == null) {
                Integer existing = names.get(company.getName());
                if(existing != null) {
                    throw new IllegalArgumentException("Company with ID "+id+" is new but Company name already exists: " + company.getName());
                }

                ProductionCompany inserted = new ProductionCompany(company);
                inserted.setVersion(1);

                companies.put(id, inserted);
                names.put(inserted.getName(), id);
                return;
            }

            // OPTIMISTIC LOCK CHECK
            if(current.getVersion() != company.getVersion()) {
                throw new OptimisticLockingFailureException(
                    "Company " + id +
                    " version mismatch. Expected " +
                    company.getVersion() +
                    " but found " +
                    current.getVersion()
                );
            }
            ProductionCompany updated = new ProductionCompany(company);
            updated.setVersion(company.getVersion() + 1);

            // HANDLE RENAME
            if(!current.getName().equals(updated.getName())) {
                Integer existing = names.get(updated.getName());
                if(existing != null && !existing.equals(id)) {
                    throw new IllegalArgumentException("Company name already exists in another company: " + updated.getName());
                }

                names.remove(current.getName());
                names.put(updated.getName(), id);
            }
            companies.put(id, updated);
        }
    }

    private int parseID(String ID)
    {
        try {
            return Integer.parseInt(ID);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid company ID: " + ID);
        }
    }
}