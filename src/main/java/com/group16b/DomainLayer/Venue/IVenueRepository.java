package com.group16b.DomainLayer.Venue;

import java.util.List;

public interface IVenueRepository {

	Venue getVenueByID(String venueID);

	void reserveTickets(String venueId, String segmentId, List<String> seatIds, int eventID);
	void reserveTickets(String venueId, String segmentId, int quantity, int eventID);

}
