package com.group16b.ApplicationLayer.Interfaces;

import java.io.IOException;

import com.group16b.DomainLayer.Venue.Location;

public interface ILocatoinService {
    

    public Location search(String location) throws IOException, InterruptedException; //limit 1
}
