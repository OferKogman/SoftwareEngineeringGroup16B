package com.group16b.InfrastructureLayer.MapDBs;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.DomainLayer.Venue.ReservationRequest;
import com.group16b.DomainLayer.Venue.Venue;


public class VenueRepositoryMapImpl implements IVenueRepository{

    private final static VenueRepositoryMapImpl instance = new VenueRepositoryMapImpl();
	private Map<String, Venue> venus = new ConcurrentHashMap<>();

	private VenueRepositoryMapImpl() {
	}

	public static VenueRepositoryMapImpl getInstance() {
		return instance;
	}

    @Override
    public void saveVenue(String venueID, Venue venue) {
        if (venueID == null || venueID.trim().isEmpty()) {
            throw new IllegalArgumentException("Venue ID cannot be null or empty.");
        }
        
        if (venue == null) {
            throw new IllegalArgumentException("Cannot save a null Venue.");
        }

        if (venus.putIfAbsent(venueID, venue) != null) {
            throw new IllegalArgumentException("A venue with ID '" + venueID + "' already exists in the database.");
        }
    }

    @Override
    public Venue getVenueByID(String venueID) {
        if (venueID == null || venueID.trim().isEmpty()) {
            throw new IllegalArgumentException("Venue ID cannot be null or empty.");
        }
        
        return venus.get(venueID);
    }

    @Override
    public void reserveTickets(String venueId, String segmentId, List<String> seatIds, int eventID) {
        Venue venue = venus.get(venueId);
        if (venue == null) {
            throw new IllegalArgumentException("Venue with ID " + venueId + " not found");
        }
        venue.reserveSeats(ReservationRequest.forSeats(eventID, seatIds, segmentId));
    }

    @Override
    public void freeTickets(String venueId, String segmentId, List<String> seatIds, int eventID) {
        Venue venue = venus.get(venueId);
        if (venue == null) {
            throw new IllegalArgumentException("Venue with ID " + venueId + " not found");
        }
        venue.freeSeats(ReservationRequest.forSeats(eventID, seatIds, segmentId));
    }
    @Override
    public void freeTickets(String venueId, String segmentId, int quantity, int eventID) {
        Venue venue = venus.get(venueId);
        if (venue == null) {
            throw new IllegalArgumentException("Venue with ID " + venueId + " not found");
        }
        venue.freeSeats(ReservationRequest.forField(eventID, quantity, segmentId));
    }

    @Override
    public void reserveTickets(String venueId, String segmentId, int quantity, int eventID) {
        Venue venue = venus.get(venueId);
        if (venue == null) {
            throw new IllegalArgumentException("Venue with ID " + venueId + " not found");
        }
        venue.reserveSeats(ReservationRequest.forField(eventID, quantity, segmentId));
    }

    @Override
    public void addVenue(String venueID, Venue venue) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addVenue'");
    }

}
