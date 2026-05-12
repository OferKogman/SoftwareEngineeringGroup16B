package com.group16b.ApplicationLayer.DTOs;
import  com.group16b.DomainLayer.Venue.Location;

public class LocationDTO {
    private String name;
    private String houseNumber;
    private String street;
    private String city;
    private String state;
    private String country;
    private Double latitude;
    private Double longitude;

    public LocationDTO(Location location) {
        this.name = location.getName();
        this.houseNumber = location.getHouseNumber();
        this.street = location.getStreet();
        this.city = location.getCity();
        this.state = location.getState();
        this.country = location.getCountry();
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
    }

    public String getName() {
         return name; 
    }

    public void setName(String name) { 
        this.name = name; 
    }

    public String getHouseNumber() { 
        return houseNumber; 
    }

    public void setHouseNumber(String houseNumber) { 
        this.houseNumber = houseNumber; 
    }

    public String getStreet() { 
        return street; 
    }

    public void setStreet(String street) { 
        this.street = street; 
    }

    public String getCity() { 
        return city; 
    }

    public void setCity(String city) { 
        this.city = city; 
    }

    public String getState() { 
        return state; 
    }

    public void setState(String state) { 
        this.state = state; 
    }

    public String getCountry() { 
        return country; 
    }
    
    public void setCountry(String country) { 
        this.country = country; 
    }

    public Double getLatitude() { 
        return latitude; 
    }

    public void setLatitude(Double latitude) { 
        this.latitude = latitude; 
    }

    public Double getLongitude() { 
        return longitude; 
    }

    public void setLongitude(Double longitude) { 
        this.longitude = longitude; 
    }
}