package com.group16b.DomainLayer.Venue;

public class Location {
    private String name;
    private String houseNumber;
    private String street;
    private String city;
    private String state;
    private String country;
    private Double latitude;
    private Double longitude;

    public Location(String name, String houseNumber, String street, String city, String state, String country, Double latitude, Double longitude) {
        this.name = name;
        this.houseNumber = houseNumber;
        this.street = street;
        this.city = city;
        this.state = state;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Location other)) {
            return false;
        }

        return java.util.Objects.equals(name, other.name)
                && java.util.Objects.equals(houseNumber, other.houseNumber)
                && java.util.Objects.equals(street, other.street)
                && java.util.Objects.equals(city, other.city)
                && java.util.Objects.equals(state, other.state)
                && java.util.Objects.equals(country, other.country);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(name, houseNumber, street, city, state, country);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (name != null && !name.isBlank()) {
            sb.append(name);
        }

        if (street != null && !street.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }

            if (houseNumber != null && !houseNumber.isBlank()) {
                sb.append(houseNumber).append(" ");
            }

            sb.append(street);
        }

        if (city != null && !city.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(city);
        }

        if (state != null && !state.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(state);
        }

        if (country != null && !country.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(country);
        }

        return sb.toString();
    }
}
