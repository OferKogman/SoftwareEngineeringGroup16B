package com.group16b.InfrastructureLayer.Adapters;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.group16b.DomainLayer.ProductionCompany.IProductionCompanyRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.User.User;
import com.group16b.InfrastructureLayer.Database.ProductionCompanyRepository;

@Repository
@Primary 
public class ProductionCompanyRepositoryAdapter implements IProductionCompanyRepository {

    private final ProductionCompanyRepository springRepo;

    public ProductionCompanyRepositoryAdapter(ProductionCompanyRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public List<ProductionCompany> getAll() {
        return springRepo.findAll();
    }

    @Override
    public ProductionCompany findByID(String id) {
        try {
            return springRepo.findById(Integer.parseInt(id)).get(); 
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("Production Company with ID " + id + " not found.");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Production Company ID must be a valid integer.");
        }
    }

    @Override
    public void save(ProductionCompany company) {
        // If the version is 0  this is a brand new company
        // we need to intercept the in memory constructor IDgen which isnt resurrected properly
        if (company.getVersion() == 0) { 
            company.setProductionCompanyId(0);
        }

        springRepo.save(company);
    }

    @Override
    public void delete(String id) {
        try {
            springRepo.deleteById(Integer.parseInt(id));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Production Company ID must be a valid integer.");
        }
    }

    @Override
    public int getIDByName(String name) {
        ProductionCompany company = springRepo.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Company with name '" + name + "' not found."));
        
        return company.getProductionCompanyID(); 
    }


    @Override
    public List<Integer> getAllUserComapnies(User user) {
        String userId = user.getEmail(); 

        return springRepo.findCompanyIdsManagedByUser(userId); 
    }

    @Override
    public List<ProductionCompany> findCompaniesManagedByUser(String userId) {
        return springRepo.findCompaniesManagedByUser(userId);
    }
}