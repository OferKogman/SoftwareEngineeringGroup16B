package com.group16b.InfrastructureLayer.Database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.group16b.DomainLayer.User.User;

@Repository
public interface UserRepository extends JpaRepository<User, String>{
    
}