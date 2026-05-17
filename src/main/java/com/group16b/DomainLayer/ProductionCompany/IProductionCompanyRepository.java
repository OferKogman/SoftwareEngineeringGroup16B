package com.group16b.DomainLayer.ProductionCompany;

import java.util.List;

import com.group16b.DomainLayer.Interfaces.IRepository;

public interface IProductionCompanyRepository extends IRepository<ProductionCompany> {
    int getIDByName(String name);

    List<Integer> getAllUserComapnies(String user);
}
