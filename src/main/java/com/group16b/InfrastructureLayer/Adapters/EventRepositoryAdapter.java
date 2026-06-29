package com.group16b.InfrastructureLayer.Adapters;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.InfrastructureLayer.Database.EventRepository;

@Repository
@Primary
public class EventRepositoryAdapter implements IEventRepository {
    private final EventRepository springRepo;

    public EventRepositoryAdapter(EventRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public List<Event> getAll() {
        return springRepo.findAll(); 
    }

    @Override
    public Event findByID(String id) {
        return springRepo.findById(parseID(id)).orElseThrow(() -> 
            new IllegalArgumentException("Event with ID " + id + " not found.")
        );
    }
    
    @Override
    public void save(Event eve) {
        springRepo.save(eve);
    }

    @Override
    public void delete(String id) {
        springRepo.deleteById(parseID(id)); 
    }

    //functon from the devil
    @Override
    public List<Event> searchEvents(
            List<String> name,
            List<String> artist,
            List<String> category,
            List<String> keyword,
            List<Number> minPrice,
            List<Number> maxPrice,
            List<LocalDateTime> startTime,
            List<LocalDateTime> endTime,
            List<Number> eventRating,
            List<Integer> productionCompanyID) {

        Specification<Event> spec = Specification.where(null);

        if (name != null && !name.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    root.get("name").in(name));
        }

        if (artist != null && !artist.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    root.get("artist").in(artist));
        }

        if (category != null && !category.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    root.get("category").in(category));
        }

        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                String pattern = "%" + keyword.get(0) + "%";
                return cb.like(root.get("name"), pattern); // you can expand later
            });
        }

        if (minPrice != null && !minPrice.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.ge(root.get("price"), minPrice.get(0).doubleValue()));
        }

        if (maxPrice != null && !maxPrice.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.le(root.get("price"), maxPrice.get(0).doubleValue()));
        }

        if (startTime != null && !startTime.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("endTime"), startTime.get(0)));
        }

        if (endTime != null && !endTime.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("startTime"), endTime.get(0)));
        }

        if (eventRating != null && !eventRating.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.ge(root.get("rating"), eventRating.get(0).doubleValue()));
        }

        if (productionCompanyID != null && !productionCompanyID.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    root.get("productionCompanyID").in(productionCompanyID));
        }

        return springRepo.findAll(spec);
    }

    @Override
	public List<Event> findAllByVenueID(String venueID)
    {
        return springRepo.findAllByVenueID(venueID);
    }
    
    private int parseID(String ID) {
		try {
			return Integer.parseInt(ID);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid Event ID: " + ID);
		}
	}
}
