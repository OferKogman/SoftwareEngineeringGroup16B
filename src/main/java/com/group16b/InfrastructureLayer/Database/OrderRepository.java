package com.group16b.InfrastructureLayer.Database;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.group16b.DomainLayer.Order.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    // Spring auto-generates these queries
    List<Order> findByEventId(int eventId);
    List<Order> findBySubjectID(String subjectID);

    @Query("SELECT o FROM Order o WHERE o.eventId = :eventId AND o.segmentId = :segmentId AND o.state = 'COMPLETED'")
    List<Order> getCompletedByEventIdField(@Param("eventId") int eventId, @Param("segmentId") String segmentId);

    @Query("SELECT o FROM Order o JOIN o.seats s WHERE o.eventId = :eventId AND o.segmentId = :segmentId AND s IN :seatIds AND o.state = 'COMPLETED'")
    List<Order> getCompletedByEventIdSeatIds(@Param("eventId") int eventId, @Param("segmentId") String segmentId, @Param("seatIds") List<String> seatIds);

    @Query("SELECT o FROM Order o WHERE o.subjectID = :subjectID AND o.state LIKE 'ACTIVE:%'")
    Optional<Order> findFirstBySubjectIDAndActiveTrue(@Param("subjectID") String subjectID);

    @Query("SELECT o FROM Order o WHERE o.subjectID = :subjectID AND o.state NOT LIKE 'ACTIVE:%'")
    List<Order> findBySubjectIDAndActiveFalse(@Param("subjectID") String subjectID);
}