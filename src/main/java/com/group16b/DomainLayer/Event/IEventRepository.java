package com.group16b.DomainLayer.Event;

public interface IEventRepository {
	// event must not be null and must have a unique ID
	public void addEvent(Event e);
	//event or null if no event with the given ID exists
	public Event getEventByID(int eventID);
	//returns true if an event with the given ID exists, false otherwise
	public boolean EventExists(int eventID);
}
