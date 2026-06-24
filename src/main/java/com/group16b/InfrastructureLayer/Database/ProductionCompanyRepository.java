package com.group16b.InfrastructureLayer.Database;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
@Repository
public interface ProductionCompanyRepository extends JpaRepository<ProductionCompany, Long> {

    @Query("SELECT p FROM ProductionCompany p JOIN p.membersNodes m WHERE KEY(m) = :userId")
    List<ProductionCompany> findCompaniesManagedByUser(@Param("userId") String userId);
}
