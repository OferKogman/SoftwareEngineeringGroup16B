package com.group16b.InfrastructureLayer.Database;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
@Repository
public interface ProductionCompanyRepository extends JpaRepository<ProductionCompany, Integer> {

    //spring generates the SQL for this automatically based on the method name!
    Optional<ProductionCompany> findByName(String name);

    @Query("SELECT p.productionCompanyID FROM ProductionCompany p JOIN p.membersNodes m WHERE KEY(m) = :userId")
    List<Integer> findCompanyIdsManagedByUser(@Param("userId") String userId);

    // 2. The full object query (this is the one your adapter is complaining about right now!)
    @Query("SELECT p FROM ProductionCompany p JOIN p.membersNodes m WHERE KEY(m) = :userId")
    List<ProductionCompany> findCompaniesManagedByUser(@Param("userId") String userId);
}
