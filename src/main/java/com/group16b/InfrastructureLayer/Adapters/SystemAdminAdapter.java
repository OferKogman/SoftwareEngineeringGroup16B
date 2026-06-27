package com.group16b.InfrastructureLayer.Adapters;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;
import com.group16b.InfrastructureLayer.Database.SystemAdminRepository;

@Repository
@Primary
public class SystemAdminAdapter  implements IRepository<SystemAdmin> {
    private final SystemAdminRepository springRepo;
    
    public SystemAdminAdapter(SystemAdminRepository springRepo){
        this.springRepo = springRepo;        
    }

    @Override
    public List<SystemAdmin> getAll() {
        return springRepo.findAll(); 
    }

    @Override
    public SystemAdmin findByID(String id) {
        return springRepo.findById(id).orElseThrow(() -> 
            new IllegalArgumentException("User with ID " + id + " not found.")
        );
    }

    @Override
    public void save(SystemAdmin user) {
        springRepo.save(user);
    }

    @Override
    public void delete(String id) {
        springRepo.deleteById(id); 
    }    
}
