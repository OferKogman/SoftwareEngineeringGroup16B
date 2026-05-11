package com.group16b.InfrastructureLayer.MapDBs;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.group16b.DomainLayer.Venue.IVenueRepository;
import com.group16b.DomainLayer.Venue.ReservationRequest;
import com.group16b.DomainLayer.Venue.Venue;


public class VenueRepositoryMapImpl implements IVenueRepository{

    private final static VenueRepositoryMapImpl instance = new VenueRepositoryMapImpl();
	private Map<String, Venue> venus = new TreeMap<>();

	private VenueRepositoryMapImpl() {
	}

	public static VenueRepositoryMapImpl getInstance() {
		return instance;
	}

    @Override
    public Venue getVenueByID(String venueID) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getVenueByID'");
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

}
