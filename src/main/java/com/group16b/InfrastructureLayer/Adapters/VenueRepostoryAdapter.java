package com.group16b.InfrastructureLayer.Adapters;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.InfrastructureLayer.Database.VenueRepository;

@Component
@Primary
@Transactional
public class VenueRepostoryAdapter implements IRepository<Venue> {
    private final VenueRepository springRepo;

    public VenueRepostoryAdapter(VenueRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public List<Venue> getAll() {
        return springRepo.findAll();
    }

    @Override
    public Venue findByID(String id) {
        return springRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venue with ID " + id + " not found."));
    }

    @Override
    public void save(Venue ven) {
        springRepo.save(ven);
    }

    @Override
    public void delete(String id) {
        springRepo.deleteById(id);
    }

}
