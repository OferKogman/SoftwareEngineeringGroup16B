package com.group16b.DomainLayer.ProductionCompanyPolicy;

import java.util.Set;

import com.group16b.DomainLayer.Policies.DiscountPolicy;
import com.group16b.DomainLayer.Policies.PurchasePolicy.PurchasePolicy;

public interface  IProductionCompanyPolicyRepository {
    public ProductionCompanyPolicy getProductionCompanyByName(String name);

    public ProductionCompanyPolicy getProductionCompanyByID(int id);
    public Set<PurchasePolicy> getPurchasePolicyByID(int productionCompanyId);
    public Set<DiscountPolicy> getDiscountPolicyByID(int productionCompanyId);
}
