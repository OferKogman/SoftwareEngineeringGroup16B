package com.group16b.InfrastructureLayer.MapDBs;

import java.util.Set;

import com.group16b.DomainLayer.Policies.DiscountPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;
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

    public void removeProductionCompany(int productionCompanyId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeProductionCompany'");
    }

    @Override
    public Set<PurchasePolicy> getPurchasePolicyByID(int productionCompanyId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPurchasePolicyByID'");
    }

    @Override
    public Set<DiscountPolicy> getDiscountPolicyByID(int productionCompanyId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDiscountPolicyByID'");
    }
}
