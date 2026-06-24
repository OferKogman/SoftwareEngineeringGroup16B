package com.group16b.InfrastructureLayer.Database;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.group16b.DomainLayer.VirtualQueue.VirtualQueue;

import jakarta.persistence.LockModeType;

@Repository
public interface VirtualQueueRepository extends JpaRepository<VirtualQueue, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM VirtualQueue v WHERE v.id = :id")
    Optional<VirtualQueue> findByIdWithPessimisticLock(@Param("id") int id);
}