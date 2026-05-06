package com.group16b.DomainLayer.Event;

public interface IEventRepository {
	public void addEvent(Event e);
	public Event getEventByID(int eventID);
	public boolean EventExists(int eventID);
}
