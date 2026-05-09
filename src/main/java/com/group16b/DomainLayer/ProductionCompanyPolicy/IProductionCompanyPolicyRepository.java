package com.group16b.DomainLayer.ProductionCompanyPolicy;

public interface  IProductionCompanyPolicyRepository {
    public ProductionCompanyPolicy getProductionCompanyByName(String name);

    public ProductionCompanyPolicy getProductionCompanyByID(int id);
}
