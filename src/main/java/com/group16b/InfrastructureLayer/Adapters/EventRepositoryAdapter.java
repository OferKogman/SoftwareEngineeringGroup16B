package com.group16b.InfrastructureLayer.Adapters;

import java.time.LocalDateTime;
import java.util.List;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import org.springframework.context.annotation.Primary;
import com.group16b.InfrastructureLayer.Database.EventRepositroy;

import org.springframework.stereotype.Component;

@Component
@Primary
public class EventRepositoryAdapter implements IEventRepository {
    private final EventRepositroy springRepo;    

    public EventRepositoryAdapter(EventRepositroy springRepo){
        this.springRepo = springRepo;
    }

    @Override
    public List<Event> getAll() {
        return springRepo.findAll(); 
    }

    @Override
    public Event findByID(String id) {
        return springRepo.findById(id).orElseThrow(() -> 
            new IllegalArgumentException("Event with ID " + id + " not found.")
        );
    }

    @Override
    public void save(Event event) {
        springRepo.save(event);
    }

    @Override
    public void delete(String id) {
        springRepo.deleteById(id); 
    }    

@Override
    public List<Event> searchEvents(List<String> name, List<String> artist, List<String> category, List<String> keyword,
            List<Number> minPrice, List<Number> maxPrice, List<LocalDateTime> startTime, List<LocalDateTime> endTime,
            List<Number> eventRating, List<Integer> productionCompanyID) {

        if (minPrice != null && !minPrice.isEmpty() && minPrice.size() != 1) {
            throw new IllegalArgumentException("Min price filter must have exactly one value.");
        }
        if (maxPrice != null && !maxPrice.isEmpty() && maxPrice.size() != 1) {
            throw new IllegalArgumentException("Max price filter must have exactly one value.");
        }
        if (startTime != null && !startTime.isEmpty() && startTime.size() != 1) {
            throw new IllegalArgumentException("Start time filter must have exactly one value.");
        }
        if (endTime != null && !endTime.isEmpty() && endTime.size() != 1) {
            throw new IllegalArgumentException("End time filter must have exactly one value.");
        }
        if (eventRating != null && !eventRating.isEmpty() && eventRating.size() != 1) {
            throw new IllegalArgumentException("Event rating filter must have exactly one value.");
        }

Double parsedMinPrice = (minPrice != null && !minPrice.isEmpty()) ? minPrice.get(0).doubleValue() : 0.0;
        Double parsedMaxPrice = (maxPrice != null && !maxPrice.isEmpty()) ? maxPrice.get(0).doubleValue() : 1000000.0;
        LocalDateTime parsedStartTime = (startTime != null && !startTime.isEmpty()) ? startTime.get(0) : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime parsedEndTime = (endTime != null && !endTime.isEmpty()) ? endTime.get(0) : LocalDateTime.of(2100, 1, 1, 0, 0);
        Double parsedRating = (eventRating != null && !eventRating.isEmpty()) ? eventRating.get(0).doubleValue() : 0.0;

        // 2. The Default Strategy for Keyword (Empty string bypasses the LIKE check!)
        String parsedKeyword = "";
        if (keyword != null && !keyword.isEmpty()) {
            parsedKeyword = "%" + keyword.get(0).toLowerCase() + "%";
        }

        // 3. Explicitly set lists to null if they are empty to prevent "IN ()" SQL syntax crashes
        List<String> parsedNames = (name != null && name.isEmpty()) ? null : name;
        List<String> parsedArtists = (artist != null && artist.isEmpty()) ? null : artist;
        List<String> parsedCategories = (category != null && category.isEmpty()) ? null : category;
        List<Integer> parsedPcIDs = (productionCompanyID != null && productionCompanyID.isEmpty()) ? null : productionCompanyID;

        // 4. Pass the bulletproof data to JPA
        return springRepo.searchEvents(
                parsedNames, 
                parsedArtists, 
                parsedCategories, 
                parsedKeyword, 
                parsedMinPrice, 
                parsedMaxPrice, 
                parsedStartTime, 
                parsedEndTime, 
                parsedRating, 
                parsedPcIDs
        );
    }

    @Override
    public List<Event> findAllByVenueID(String venueID) {
        return springRepo.findAllByVenueID(venueID);
    }         
}
