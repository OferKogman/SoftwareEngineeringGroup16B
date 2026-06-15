package com.group16b.InfrastructureLayer.Database;

import com.group16b.DomainLayer.Venue.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VenueRepository extends JpaRepository<Venue, String> {
}