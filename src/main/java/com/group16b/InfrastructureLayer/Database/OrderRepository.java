package com.group16b.InfrastructureLayer.Database;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.group16b.DomainLayer.Order.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.subjectID = :userId AND CAST(o.state AS string) NOT LIKE '%ActiveOrder%'")
    List<Order> findByUserIdAndActiveFalse(String userId);

    @Query(value = "SELECT * FROM orders WHERE subjectid = :userId AND state LIKE '%ActiveOrder%' LIMIT 1", nativeQuery = true)
    Optional<Order> findFirstByUserIdAndActiveTrue(String userId);
}