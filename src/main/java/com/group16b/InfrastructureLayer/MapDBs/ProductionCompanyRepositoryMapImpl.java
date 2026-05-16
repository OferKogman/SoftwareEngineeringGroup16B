package com.group16b.InfrastructureLayer.MapDBs;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import org.springframework.dao.OptimisticLockingFailureException;
public class ProductionCompanyRepositoryMapImpl implements IRepository<ProductionCompany> {
    private final ConcurrentHashMap<Integer, ProductionCompany> companies = new ConcurrentHashMap<>();
    
    public ProductionCompanyRepositoryMapImpl()
    {
    }
    
    //return the company if found, if not found throw execption
    public ProductionCompany findByID(String ID)
    {
        ProductionCompany company=companies.get(Integer.parseInt(ID));
        if(company==null)
            throw new IllegalArgumentException("Production company with ID "+ID+" is not found.");
        return new ProductionCompany(company);
    }

    public List<ProductionCompany> getAl()
    {
        return companies.values().stream().map(ProductionCompany::new).toList();
    }

    public void delete(String ID)
    {
        companies.remove(Integer.parseInt(ID));
    }
    public void save(ProductionCompany company)
    {
        int id = company.getProductionCompanyID();
        companies.compute(id, (key, current) -> {
            if (current == null)
            {//save this version as new
                ProductionCompany inserted = new ProductionCompany(company);
                inserted.setVerson(1);
                return inserted;
            }

            if (current.getVersion() != company.getVersion())
            {//mismatch
                throw new OptimisticLockingFailureException(
                    "Company " + id +
                    " version mismatch. Expected " +
                    company.getVersion() +
                    " but found " +
                    current.getVersion()
                );
            }

            //match so move to update
            ProductionCompany updated = new ProductionCompany(company);
            updated.setVerson(company.getVersion() + 1);

            return updated;
        });
    }

   
}
