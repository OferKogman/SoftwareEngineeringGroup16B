package com.group16b.InfrastructureLayer.Database;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.group16b.DomainLayer.Event.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer>,JpaSpecificationExecutor<Event>{
    List<Event> findAllByVenueID(String venueID);
}
