package com.group16b.DomainLayer.Venue;

public class Location {
    private String displayName;
    private String name;
    private String houseNumber;
    private String street;
    private String city;
    private String state;
    private String country;
    private Double latitude;
    private Double longitude;

    public Location(String displayName, String name, String houseNumber, String street, String city, String state, String country, Double latitude, Double longitude) {
        this.displayName = displayName;
        this.name = name;
        this.houseNumber = houseNumber;
        this.street = street;
        this.city = city;
        this.state = state;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getName() {
        return name;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public boolean matches(Location l1) {
        return l1.getCity().equals(this.getCity()) && l1.getCountry().equals(this.getCountry());
    }
}
