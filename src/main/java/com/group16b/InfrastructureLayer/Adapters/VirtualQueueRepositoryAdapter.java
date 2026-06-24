package com.group16b.InfrastructureLayer.Adapters;

import com.group16b.DomainLayer.VirtualQueue.VirtualQueue;
import com.group16b.InfrastructureLayer.Database.VirtualQueueRepository;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.group16b.DomainLayer.Interfaces.IRepository;

@Repository
@Primary 
public class VirtualQueueRepositoryAdapter implements IRepository<VirtualQueue> {

    private final VirtualQueueRepository springRepo;

    public VirtualQueueRepositoryAdapter(VirtualQueueRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public VirtualQueue findByID(String id) {
        try {
            int parsedId = Integer.parseInt(id);
            return springRepo.findByIdWithPessimisticLock(parsedId).orElse(null);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("VirtualQueue ID must be a number, received: " + id);
        }
    }

    @Override
    public void save(VirtualQueue queue) {
        springRepo.save(queue);
    }
    @Override
    public List<VirtualQueue> getAll() {
        return springRepo.findAll();
    }

    @Override
    public void delete(String id) {
        try {
            int parsedId = Integer.parseInt(id);
            springRepo.deleteById(parsedId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("VirtualQueue ID must be a number, received: " + id);
        }
    }
}