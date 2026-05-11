package com.group16b.InfrastructureLayer.MapDBs;

import com.group16b.DomainLayer.ProductionCompanyPolicy.IProductionCompanyPolicyRepository;
import com.group16b.DomainLayer.ProductionCompanyPolicy.ProductionCompanyPolicy;

public class ProductionCompanyPolicyRepositoryMapImpl implements IProductionCompanyPolicyRepository {

    private static final ProductionCompanyPolicyRepositoryMapImpl instance = new ProductionCompanyPolicyRepositoryMapImpl();
    
    public static synchronized ProductionCompanyPolicyRepositoryMapImpl getInstance() {
		return instance;
	}

    @Override
    public ProductionCompanyPolicy getProductionCompanyByName(String name){
        // Implementation for retrieving production company by name
        return null;
    }

    @Override
    public ProductionCompanyPolicy getProductionCompanyByID(int id){
        // Implementation for retrieving production company by ID
        return null;
    }
}
