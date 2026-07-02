package com.group16b.DomainLayer.ProductionCompany;

import java.util.List;

import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.User.User;

public interface IProductionCompanyRepository extends IRepository<ProductionCompany> {
    int getIDByName(String name);

    List<Integer> getAllUserComapnies(User user);
    List<ProductionCompany> findCompaniesManagedByUser(String Id);

    int getLatestCompanyID();
}
