package com.group16b.InfrastructureLayer.Database;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.group16b.DomainLayer.Event.Event;

@Repository
public interface EventRepositroy extends JpaRepository<Event, String> {
    @Query("SELECT e FROM Event e WHERE e.venueID = :venueID")
    List<Event> findAllByVenueID(@Param("venueID") String venueID);

    @Query("SELECT e FROM Event e WHERE " +
           "(:names IS NULL OR e.name IN :names) AND " +
           "(:artists IS NULL OR e.artist IN :artists) AND " +
           "(:categories IS NULL OR e.category IN :categories) AND " +
           "(e.price >= :minPrice) AND " +
           "(e.price <= :maxPrice) AND " +
           "(e.endTime >= :startTime) AND " +
           "(e.startTime <= :endTime) AND " +
           "(e.rating >= :rating) AND " +
           "(:pcIDs IS NULL OR e.productionCompanyID IN :pcIDs) AND " +
           "(:keyword = '' OR LOWER(e.name) LIKE :keyword OR LOWER(e.artist) LIKE :keyword OR LOWER(e.category) LIKE :keyword)")
    List<Event> searchEvents(
            @Param("names") List<String> names,
            @Param("artists") List<String> artists,
            @Param("categories") List<String> categories,
            @Param("keyword") String keyword,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("rating") Double rating,
            @Param("pcIDs") List<Integer> pcIDs);

}
