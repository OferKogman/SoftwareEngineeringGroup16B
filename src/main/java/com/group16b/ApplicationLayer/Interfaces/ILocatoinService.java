package com.group16b.ApplicationLayer.Interfaces;

import com.group16b.DomainLayer.Venue.Location;

public interface ILocatoinService {
    

    public Location search(String location); //limit 1
}
