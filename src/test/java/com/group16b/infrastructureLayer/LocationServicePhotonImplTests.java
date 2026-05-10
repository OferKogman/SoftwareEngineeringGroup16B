package com.group16b.infrastructureLayer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.group16b.ApplicationLayer.Interfaces.ILocatoinService;
import com.group16b.DomainLayer.Venue.Location;
import com.group16b.InfrastructureLayer.LocationServicePhotonImpl;

public class LocationServicePhotonImplTests {
    
    private ILocatoinService locationService = new LocationServicePhotonImpl();

    @Test
    public void SuccessfullLocationSearch() {
        try {
            Location loc = locationService.search("London");
            assertEquals(new Location("London", null, null, null, "England", "United Kingdom", 51.5074456, -0.1277653), loc);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void FailEmptyLocation() {
        try {
            Location loc = locationService.search("");
        }
        catch (IllegalArgumentException e) {
            assertEquals("location cannot be empty or null", e.getMessage());
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void FailNullLocation() {
        try {
            Location loc = locationService.search(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("location cannot be empty or null", e.getMessage());
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
