package com.group16b.InfrastructureLayer.Database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.group16b.DomainLayer.Venue.Venue;

@Repository
public interface VenueRepository extends JpaRepository<Venue,String>{
    
}
