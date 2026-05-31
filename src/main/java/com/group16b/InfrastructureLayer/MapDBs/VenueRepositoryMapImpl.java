package com.group16b.InfrastructureLayer.MapDBs;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Venue.Venue;

    // T findByID(String ID);
    // List<T> getAll();
    // void delete(String ID);
    // void save(T Obj);
public class VenueRepositoryMapImpl implements IRepository<Venue>{

    private final ConcurrentHashMap<String, Venue> venues = new ConcurrentHashMap<>();

	public VenueRepositoryMapImpl() {
	}

    public Venue findByID(String id){
        Venue venue = venues.get(id);
        if(venue == null){
            throw new IllegalArgumentException("No venue found for id: " + id);
        } 
        return new Venue(venue);
    }

    public synchronized void save(Venue venue){
        Venue currentVenue = venues.get(venue.getID());
        //here is the case of venue is addedd
        if (currentVenue == null) {
            Venue newVenue = new Venue(venue);
            newVenue.setVersion(1);//start at 1

            venues.putIfAbsent(venue.getID(), newVenue);
            return; 
        }
        
        //case of updating existing version
        long newVersion = venue.getVersion();
        long currentVersion = currentVenue.getVersion();
        if (newVersion != currentVersion) {
				throw new IllegalArgumentException("Version mismatch: expected " + currentVersion + " but got " + newVersion);
        }
        Venue updatedVenue = new Venue(venue);
        updatedVenue.setVersion(venue.getVersion() + 1);

        boolean replaced = venues.replace(venue.getID(), currentVenue, updatedVenue);
        if(!replaced){
            throw new IllegalArgumentException(
                "Venue " + venue.getID() + " version mismatch or concurrent modification, Expected version " + 
                venue.getVersion() + ", but found " + currentVenue.getVersion() + " at read time (actual DB might be higher now"
            );
        }
    }

    @Override
	public synchronized void delete(String ID) {
		Venue venue = venues.get(ID);
		if (venue == null) {
			throw new IllegalArgumentException("No venue found to delete for id: " + ID);
		}
        venues.remove(ID);
	}    

    @Override
	public List<Venue> getAll() {
		List<Venue> venuesToList = new LinkedList<>();
		for(Venue venue : venues.values()) {
			venuesToList.add(new Venue(venue));
		}
		return venuesToList;
	}


}
