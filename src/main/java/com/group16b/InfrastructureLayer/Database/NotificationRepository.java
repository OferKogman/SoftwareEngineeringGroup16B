package com.group16b.InfrastructureLayer.Database;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.group16b.DomainLayer.Notification;

public interface NotificationRepository extends JpaRepository<Notification, String> {

    @Query("""
            SELECT n
            FROM Notification n
            WHERE n.userID = :userID
              AND n.sent = false
            ORDER BY n.timestamp ASC
            """)
    List<Notification> findUnsentByUserID(@Param("userID") String userID);
}