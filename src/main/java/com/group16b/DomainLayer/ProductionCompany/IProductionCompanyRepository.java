package com.group16b.DomainLayer.ProductionCompany;

import com.group16b.DomainLayer.Interfaces.IRepository;

public interface IProductionCompanyRepository extends IRepository<ProductionCompany> {
    int getIDByName(String name);
}
