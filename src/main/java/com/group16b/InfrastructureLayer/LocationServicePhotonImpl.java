package com.group16b.InfrastructureLayer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group16b.ApplicationLayer.Interfaces.ILocatoinService;
import com.group16b.DomainLayer.Venue.Location;

public class LocationServicePhotonImpl implements ILocatoinService {

    @Override
    public Location search(String location) throws IOException, InterruptedException {
        if (location == null || location == "") {
            throw new IllegalArgumentException("location cannot be empty or null");
        }
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://photon.komoot.io/api/?q=" + location.replace(" ", "+") + "&limit=1"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());
        JsonNode firstFeature = root.path("features").path(0);
        JsonNode result = firstFeature.path("properties");
        JsonNode coordinates = firstFeature.path("geometry").path("coordinates");

        return new Location(
                result.path("name").asText(null),
                result.path("housenumber").asText(null),
                result.path("street").asText(null),
                result.path("city").asText(null),
                result.path("state").asText(null),
                result.path("country").asText(null),
                coordinates.path(1).asDouble(),
                coordinates.path(0).asDouble());
    }

}
