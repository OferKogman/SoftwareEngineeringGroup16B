package com.group16b.InfrastructureLayer.Database;

import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemAdminRepository extends JpaRepository<SystemAdmin, String> {
}